package org.gbif.d1.mn.backend;

import java.io.InputStream;
import java.util.Date;

import javax.annotation.Nullable;

import org.dataone.ns.service.apis.v1.SystemMetadataProvider;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * The contract that a back-end must adhere to, allowing it to plug in to the REST layer.
 * <p>
 * <strong>All implementations are required to be fully threadsafe.</strong>
 */
public interface MNBackend extends SystemMetadataProvider {

  /**
   * Provides the checksum for the identified object.
   *
   * @param identifier for the object
   * @param checksumAlgorithm which the checksum should have been calculated with
   * @return the checksum or null if not found
   * @throws UnsupportedOperationException if the checksum algorithm is not supported
   */
  Checksum checksum(Identifier identifier, String checksumAlgorithm);

  /**
   * Indicates the back-end can be closed which might close resources, and flush caches.
   * Once closed, the back-end will not be reopened.
   */
  void close();

  Identifier create(Session session, Identifier pid, InputStream object, SystemMetadata sysmeta);

  /**
   * Delete the identified object from the back-end.
   *
   * @param session
   * @param pid
   * @return deleted Identifier or null if not found
   */
  Identifier delete(Session session, Identifier pid);

  /**
   * Returns a description of the identified object.
   *
   * @param identifier for the object
   * @return the description or null if not found
   */
  DescribeResponse describe(Identifier identifier);

  Identifier generateIdentifier(Session session, String scheme, String fragment);

  /**
   * Gets a stream to the identified object.
   *
   * @param identifier for the object
   * @return the stream or null if not found
   */
  InputStream get(Identifier identifier);

  /**
   * Gets a stream to the identified object.
   *
   * @param identifier for the object
   * @return the stream or null if not found
   */
  void archive(Identifier identifier);

  /**
   * Test if the client identified by the session is allowed to perform an operation at the stated permission level on
   * the specific object.
   */
  boolean isAuthorized(Session session, Identifier identifier, Permission action);

  /**
   * A health test for the back-end. The back-end should perform some minimum test to ensure that it is operational.
   * This is may be called frequently so should be a very high speed operation. A sample SQL call, or verification that
   * a file system can be read would be examples of suitable implementations.
   *
   * @return the result of the health test
   */
  Health health();

  /**
   * Performs a search filtering by the provided parameters.
   */
  ObjectList listObjects(NodeReference self, Date fromDate, @Nullable Date toDate, @Nullable String formatId,
    @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count);

  /**
   * Returns the system metadata for the identified object.
   *
   * @param identifier for the object
   * @return the metadata or null if not found
   */
  SystemMetadata systemMetadata(Identifier identifier);

  Identifier update(Session session, Identifier pid, InputStream object, Identifier newPid, SystemMetadata sysmeta);

  long getEstimateCapacity();
}
