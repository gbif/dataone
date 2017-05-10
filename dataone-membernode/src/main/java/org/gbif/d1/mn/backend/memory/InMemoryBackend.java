package org.gbif.d1.mn.backend.memory;

import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.apache.commons.codec.digest.DigestUtils;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.ObjectInfo;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * An in-memory implementation of the back-end, suitable for testing only.
 */
@ThreadSafe
public class InMemoryBackend implements MNBackend {

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  private final Object lock = new Object();

  private static Map<String, Function<InputStream, String>> CHECKSUM_FUNCTIONS =
          ImmutableMap.of(
                  "md5",
                  new Function<InputStream, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable InputStream input) {
                      try {
                        return DigestUtils.md5Hex(input);
                      } catch (IOException e) {
                        return null;
                      }
                    }
                  },
                  "sha1",
                  new Function<InputStream, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable InputStream input) {
                      try {
                        return DigestUtils.shaHex(input);
                      } catch (IOException e) {
                        return null;
                      }
                    }
                  });

  @GuardedBy("lock")
  private final LinkedHashMap<Identifier, PersistedObject> data;

  public InMemoryBackend() {
    data = Maps.newLinkedHashMap();
  }

  public InMemoryBackend(LinkedHashMap<Identifier, PersistedObject> data) {
    this.data = data;
  }

  @Override
  public Checksum checksum(Identifier pid, String checksumAlgorithm) {

    if(!CHECKSUM_FUNCTIONS.containsKey(checksumAlgorithm.toLowerCase())){
      throw new UnsupportedOperationException("Unkown checksumAlgorithm " + checksumAlgorithm);
    }

    String checksumStr = CHECKSUM_FUNCTIONS.get(checksumAlgorithm.toLowerCase()).apply(get(pid));
    if(checksumStr == null){
      throw new ServiceFailure("Unable to compute checksum");
    }

    Checksum checksum = Checksum.builder()
            .withValue(checksumStr)
            .withAlgorithm(checksumAlgorithm)
            .build();
    return checksum;
  }

  /**
   * Does nothing (by design).
   */
  @Override
  public void close() {
  }

  /**
   * Warning: Does not close the input stream.
   */
  @Override
  public Identifier create(Session session, Identifier pid, InputStream object, SystemMetadata sysmeta) {
    try {
      synchronized (lock) {
        if (data.containsKey(pid)) {
          throw new IdentifierNotUnique("Cannot create object which already exists", pid.getValue());
        } else {
          Date now = new Date();
          data.put(pid, new PersistedObject(ByteStreams.toByteArray(object), sysmeta, now, now));
          return pid;
        }
      }
    } catch (IOException e) {
      throw new ServiceFailure("Unable to read the data object from the input stream", e);
    }
  }

  @Override
  public Identifier delete(Session session, Identifier pid) {
    Identifier deletedPid = null;
    synchronized (lock) {
      if (data.remove(pid) != null) {
        deletedPid = pid;
      }
    }
    return deletedPid;
  }

  @Override
  public DescribeResponse describe(Identifier identifier) {
    return null;
  }

  @Override
  public Identifier generateIdentifier(Session session, String scheme, String fragment) {
    return null;
  }

  @Override
  public InputStream get(Identifier pid) {
    byte[] copy = null;
    synchronized (lock) {
      if (data.containsKey(pid)) {
        copy = data.get(pid).getData().clone();
      }
    }
    if (copy != null) {
      return new ByteArrayInputStream(copy);
    } else {
      return new ByteArrayInputStream(EMPTY_BYTE_ARRAY);
    }
  }

  @Override
  public SystemMetadata getSystemMetadata(Session session, Identifier pid) {
    return systemMetadata(pid);
  }

  /**
   * Always returns "healthy".
   */
  @Override
  public Health health() {
    return Health.healthy();
  }

  /**
   * Does a full scan of the in memory objects to return a list that match the criteria. Being a full scan, this is
   * hardly performance oriented, but this is not intended for anything more than testing a few objects.
   */
  @Override
  public ObjectList listObjects(NodeReference self, Date fromDate, @Nullable Date toDate, @Nullable String formatId,
    @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count) {
    ImmutableList<PersistedObject> filtered = filter(self, fromDate, toDate, formatId, replicaStatus, start, count);
    ObjectList.Builder<?> builder = ObjectList.builder();
    for (PersistedObject po : filtered) {
      SystemMetadata sysmeta = po.getSysmeta();
      Preconditions.checkState(sysmeta != null, "An object without SystemMetadata is not valid");
      builder.addObjectInfo(
        ObjectInfo.builder()
          .withChecksum(sysmeta.getChecksum())
          .withDateSysMetadataModified(sysmeta.getDateSysMetadataModified())
          .withFormatId(sysmeta.getFormatId())
          .withIdentifier(sysmeta.getIdentifier())
          .withSize(sysmeta.getSize())
          .build());
    }
    return builder.build();
  }

  @Override
  public SystemMetadata systemMetadata(Identifier pid) {
    synchronized (lock) {
      if (data.containsKey(pid)) {
        return data.get(pid).getSysmeta();
      }
    }
    return null;
  }

  @Override
  public Identifier update(Session session, Identifier pid, InputStream object, Identifier newPid,
    SystemMetadata sysmeta) {
    return null;
  }

  @VisibleForTesting
  ImmutableList<PersistedObject> filter(NodeReference self, Date fromDate, @Nullable Date toDate,
    @Nullable String formatId, @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count) {
    // set defaults
    start = start == null ? 0 : start;
    count = count == null ? 1000 : count;

    // Filter the objects to find those matching the criteria
    // the static factories in Filters returns "alwaysTrue" for null values
    ImmutableList<Predicate<PersistedObject>> filter = ImmutableList.of(
      Filters.after(fromDate),
      Filters.before(toDate),
      Filters.formatId(formatId),
      Filters.replicaStatus(replicaStatus, self)
      );
    Iterable<PersistedObject> satisfied = null; // candidate objects meeting the filter criteria
    synchronized (lock) {
      satisfied = Iterables.filter(data.values(), Predicates.and(filter));
    }

    // Build the results, taking care of the paging supplied in count and start
    ImmutableList.Builder<PersistedObject> results = ImmutableList.builder();
    Iterator<PersistedObject> iter = satisfied.iterator();
    int index = 0;
    int resultSize = 0;
    while (iter.hasNext() && resultSize < count) {
      PersistedObject object = iter.next();
      // skip until we hit the page we wish to include
      if (index++ >= start) {
        results.add(object);
        resultSize++;
      }
    }

    return results.build();
  }
}
