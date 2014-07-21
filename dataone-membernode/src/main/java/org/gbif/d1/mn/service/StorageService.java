package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import java.io.InputStream;

import com.google.common.base.Objects;
import org.dataone.ns.service.apis.v1.MNStorage;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.util.D1Preconditions.checkState;
import static org.gbif.d1.mn.util.D1Throwables.propagateOrServiceFailure;

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

  StorageService(AuthorizationManager authorizationManager, MNBackend backend) {
    this.backend = backend;
    this.authorizationManager = authorizationManager;
  }

  @Override
  public Identifier archive(Session session, Identifier pid) {
    return null;
  }

  @Override
  public Identifier create(Session session, String pid, InputStream object, SystemMetadata sysmeta) {
    // TODO: we can't do this because the object does not exist
    // authorizationManager.checkIsAuthorized(session, Identifier.builder().withValue(pid).build(), Permission.WRITE);
    checkState(Objects.equal(pid, sysmeta.getIdentifier().getValue()),
      "System metadata must have the correct identifier");
    try {
      return backend.create(session, Identifier.builder().withValue(pid).build(), object, sysmeta);
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }

  @Override
  public Identifier delete(Session session, Identifier pid) {
    authorizationManager.checkIsAuthorized(session, pid, Permission.CHANGE_PERMISSION);
    try {
      return backend.delete(session, pid);
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }

  @Override
  public Identifier generateIdentifier(Session session, String scheme, String fragment) {
    // TODO: what authorization rights?
    try {
      // TODO: how do we ensure it is globally unique?
      return backend.generateIdentifier(session, scheme, fragment);
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }

  @Override
  public Identifier update(Session session, Identifier pid, InputStream object, Identifier newPid,
    SystemMetadata sysmeta) {
    authorizationManager.checkIsAuthorized(session, pid, Permission.CHANGE_PERMISSION);
    try {
      return backend.update(session, pid, object, newPid, sysmeta);
    } catch (Throwable e) {
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }
}
