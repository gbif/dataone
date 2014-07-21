package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import com.google.common.base.Preconditions;
import org.dataone.ns.service.apis.v1.MNAuthorization;
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
final class MNAuthorizationImpl implements MNAuthorization {

  private static final Logger LOG = LoggerFactory.getLogger(MNAuthorizationImpl.class);

  private final AuthorizationManager authorizationManager;

  MNAuthorizationImpl(AuthorizationManager authorizationManager) {
    Preconditions.checkNotNull(authorizationManager, "An authorization manager is required");
    this.authorizationManager = authorizationManager;
  }

  @Override
  public boolean isAuthorized(Session session, Identifier identifier, Permission action) {
    authorizationManager.checkIsAuthorized(session, identifier, action);
    return true;
  }
}
