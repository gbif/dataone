package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.util.DataManagementLog;

import java.io.InputStream;
import java.util.Date;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import org.dataone.ns.service.apis.v1.MNRead;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.util.D1Preconditions.checkFound;
import static org.gbif.d1.mn.util.D1Throwables.propagateOrServiceFailure;

/**
 * Implements Tier 1 of the MN stack.
 * <p>
 * It is a requirement that this be constructed with thread-safe {@link Node}, {@link AuthorizationManager} and
 * {@link MNBackend}, which will make this class unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance.
 * <p>
 * TODO: add all the log event
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">DataONE Member Node API</a>
 */
final class MNReadImpl implements MNRead {

  private static final Logger LOG = LoggerFactory.getLogger(MNReadImpl.class);

  private static final String DATE_FORMAT = "HH:mm:ss Z 'on' EEE, MMM d, yyyy";
  @VisibleForTesting
  static final DateTimeFormatter DTF = DateTimeFormat.forPattern(DATE_FORMAT); // threadsafe

  private final AuthorizationManager authorizationManager;
  private final MNBackend backend;
  private final Node self;
  private final EventBus eventBus;

  MNReadImpl(Node self, AuthorizationManager authorizationManager, MNBackend backend, EventBus eventBus) {
    Preconditions.checkNotNull(self, "The self Node is required");
    Preconditions.checkNotNull(authorizationManager, "An authorization manager is required");
    Preconditions.checkNotNull(backend, "A backend is required");
    this.backend = backend;
    this.self = self;
    this.authorizationManager = authorizationManager;
    this.eventBus = eventBus;
  }

  @Override
  public DescribeResponse describe(Session session, Identifier identifier) {
    authorizationManager.checkIsAuthorized(session, identifier, Permission.READ);
    try {
      return checkFound(backend.describe(identifier), identifier, "Object not found on this node");
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }

  @Override
  public InputStream get(Session session, String pid) {
    // TODO add logging
    return getAsStream(session, Identifier.builder().withValue(pid).build());
  }

  @Override
  public Node getCapabilities() {
    return self;
  }

  @Override
  public Checksum getChecksum(Session session, Identifier identifier, String checksumAlgorithm) {
    authorizationManager.checkIsAuthorized(session, identifier, Permission.READ);
    try {
      return checkFound(backend.checksum(identifier, checksumAlgorithm), identifier, "Object not found on this node");
    } catch (UnsupportedOperationException e) {
      throw new ServiceFailure("Checksum algorithm is not supported by this implementation");
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }

  @Override
  public Log getLogRecords(Session session, Date fromDate, Date toDate, Event event, Identifier pidFilter,
    Integer start, Integer count) {
    // TODO: implement
    return null;
  }

  @Override
  public InputStream getReplica(Session session, Identifier identifier) {
    // TODO: pay attention to the logging when we add it
    return getAsStream(session, identifier);
  }

  @Override
  public SystemMetadata getSystemMetadata(Session session, Identifier identifier) {
    authorizationManager.checkIsAuthorized(session, identifier, Permission.READ);
    try {
      return checkFound(backend.systemMetadata(identifier), identifier, "Object not found on this node");
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }

  @Override
  public ObjectList listObjects(Session session, Date fromDate, @Nullable Date toDate, @Nullable String formatId,
    @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count) {
    // TODO: what do we do for the authentication here?
    return null;
  }

  @Override
  public String ping() {
    Health health = backend.health();
    if (health.isHealthy()) {
      return DTF.print(new DateTime());
    } else {
      LOG.error(health.getCause().getMessage(), health.getCause());
      throw new ServiceFailure("Unable to connect to back-end system");
    }
  }

  @Override
  public boolean synchronizationFailed(Session session, ExceptionDetail detail) {
    // TODO: probably do something with NDC or something
    DataManagementLog.error("A coordinating Node has informed us that synchronization failed: {}", detail);
    return true; // we need to acknowledge we logged this
  }

  @Override
  public boolean systemMetadataChanged(Session session, Identifier pid, long serialVersion,
    Date dateSystemMetadataLastModified) {
    Preconditions.checkNotNull(pid, "Identifier is required in the system metadata");
    Preconditions.checkNotNull(pid.getValue(), "Identifier is required in the system metadata");
    eventBus.post(new SystemMetadataUpdateEvent(pid.getValue()));
    return true;
  }

  private InputStream getAsStream(Session session, Identifier identifier) {
    authorizationManager.checkIsAuthorized(session, identifier, Permission.READ);
    try {
      return checkFound(backend.get(identifier), identifier, "Object not found on this node");
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }
}
