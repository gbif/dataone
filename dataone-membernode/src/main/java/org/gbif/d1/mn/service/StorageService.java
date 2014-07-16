package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import java.io.InputStream;

import org.dataone.ns.service.apis.v1.MNStorage;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Tier 3 of the MN stack.
 * <p>
 * It is a requirement that this be constructed with thread-safe {@link Node}, {@link AuthorizationManager} and
 * {@link MNBackend}, which will make this class unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">DataONE Member Node API</a>
 */
final class StorageService implements MNStorage {

  private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);

  private final AuthorizationManager authorizationManager;
  private final MNBackend backend;
  private final Node self;

  StorageService(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    this.backend = backend;
    this.self = self;
    this.authorizationManager = authorizationManager;
  }

  @Override
  public Identifier archive(Session session, String pid) throws InvalidToken, ServiceFailure, NotAuthorized, NotFound,
    NotImplemented {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Identifier create(Session session, String pid, InputStream object, SystemMetadata sysmeta)
    throws IdentifierNotUnique, InsufficientResources, InvalidRequest, InvalidSystemMetadata, InvalidToken,
    NotAuthorized, NotImplemented, ServiceFailure, UnsupportedType {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Identifier delete(Session session, String pid) throws InvalidToken, ServiceFailure, NotAuthorized, NotFound,
    NotImplemented {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Identifier generateIdentifier(Session session, String scheme, String fragment) throws InvalidToken,
    ServiceFailure, NotAuthorized, NotImplemented, InvalidRequest {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Identifier update(Session session, String pid, InputStream object, String newPid, SystemMetadata sysmeta)
    throws IdentifierNotUnique, InsufficientResources, InvalidRequest, InvalidSystemMetadata, InvalidToken,
    NotAuthorized, NotImplemented, ServiceFailure, UnsupportedType, NotFound {
    // TODO Auto-generated method stub
    return null;
  }
}
