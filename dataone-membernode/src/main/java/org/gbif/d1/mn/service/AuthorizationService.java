package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import java.util.Date;

import org.dataone.ns.service.apis.v1.MemberNodeAuthorization;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Tier 2 of the MN stack.
 * <p>
 * It is a requirement that this be constructed with thread-safe {@link Node}, {@link AuthorizationManager} and
 * {@link MNBackend}, which will make this class unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">DataONE Member Node API</a>
 */
final class AuthorizationService implements MemberNodeAuthorization {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizationService.class);

  private final AuthorizationManager authorizationManager;
  private final MNBackend backend;
  private final Node self;

  AuthorizationService(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    this.backend = backend;
    this.self = self;
    this.authorizationManager = authorizationManager;
  }

  @Override
  public boolean isAuthorized(Session session, String pid, Permission action) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean systemMetadataChanged(Session session, Identifier pid, long serialVersion,
    Date dateSystemMetadataLastModified) {
    // TODO Auto-generated method stub
    return false;
  }
}
