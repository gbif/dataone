package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import java.io.InputStream;
import java.util.Date;

import com.google.common.annotations.VisibleForTesting;
import org.dataone.ns.service.apis.v1.MemberNodeRead;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.SynchronizationFailed;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Tier 1 of the MN stack.
 * <p>
 * It is a requirement that this be constructed with thread-safe {@link Node}, {@link AuthorizationManager} and
 * {@link MNBackend}, which will make this class unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">DataONE Member Node API</a>
 */
final class ReadService implements MemberNodeRead {

  private static final Logger LOG = LoggerFactory.getLogger(ReadService.class);
  private static final String DATE_FORMAT = "HH:mm:ss Z 'on' EEE, MMM d, yyyy";
  @VisibleForTesting
  static final DateTimeFormatter DTF = DateTimeFormat.forPattern(DATE_FORMAT); // threadsafe

  private final AuthorizationManager authorizationManager;
  private final MNBackend backend;
  private final Node self;

  ReadService(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    this.backend = backend;
    this.self = self;
    this.authorizationManager = authorizationManager;
  }

  @Override
  public DescribeResponse describe(Session session, String pid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream get(Session session, String pid) throws InvalidToken, NotAuthorized, NotImplemented,
    ServiceFailure, NotFound, InsufficientResources {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Node getCapabilities(Session session) throws NotImplemented, ServiceFailure {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Checksum getChecksum(Session session, String pid, String checksumAlgorithm) throws InvalidRequest,
    InvalidToken, NotAuthorized, NotImplemented, ServiceFailure, NotFound {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Log getLogRecords(Session session, Date fromDate, Date toDate, Event event, String pidFilter, Integer start,
    Integer count) throws InvalidRequest, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InputStream getReplica(Session session, String pid) throws InvalidToken, NotAuthorized, NotImplemented,
    ServiceFailure, NotFound, InsufficientResources {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SystemMetadata getSystemMetadata(Session session, String identifier) throws NotAuthorized, NotFound,
    ServiceFailure, InvalidToken, InsufficientResources {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectList listObjects(Session session, Date fromDate, Date toDate, String formatId, Boolean replicaStatus,
    Integer start, Integer count) throws InvalidRequest, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String ping(Session session) throws NotImplemented, ServiceFailure, InsufficientResources {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean synchronizationFailed(Session session, SynchronizationFailed message) throws InvalidToken,
    NotAuthorized, NotImplemented, ServiceFailure {
    // TODO Auto-generated method stub
    return false;
  }
}
