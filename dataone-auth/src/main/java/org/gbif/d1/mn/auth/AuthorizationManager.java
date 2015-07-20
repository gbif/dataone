package org.gbif.d1.mn.auth;

import javax.servlet.http.HttpServletRequest;

import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

/**
 * Verifies that the given request is authorized to perform the given permission on the identified object. If the
 * request should not be authorized then an exception is thrown.
 * <p>
 * The high level procedure for this is to extract all the subjects from the session presented and then succeed if any
 * of the following conditions are met.
 * <ul>
 * <li>The object has an explicit access rule granting permission</li>
 * <li>The request originates from rights holder of the object</li>
 * <li>The request originates from the same server that is servicing the request</li>
 * <li>The request originates from known coordinating node</li>
 * </ul>
 * <p>
 * Note that some of these rules require a callback to the coordinating nodes in the DataONE network.
 *
 * All methods will throw
 * <ul>
 * <li>{@link NotAuthorized} If the checks ran to completion and the caller is not authorized<li>
 * <li>{@link NotFound} If the identified object is not found on this node<li>
 * <li>{@link ServiceFailure If an error occurs, including connecting to a coordinating node</li>
 * <li>{@link InsufficientResources If the implementation decides it is refusing access due to a resource limit</li>
 * <ul>
 * Implementations of this class must be unconditionally thread-safe and documented as such.
 */
public interface AuthorizationManager {

  /**
   * The OID for DataONE extensions which was registered with cilogon.org.
   */
  String DEFAULT_OID_SUBJECT_INFO = "1.3.6.1.4.1.34998.2.1";

  Session checkIsAuthorized(HttpServletRequest request, String identifier, Permission permission);

  Session checkIsAuthorized(HttpServletRequest request, Permission permission);

  // TODO: remove
  //void checkIsAuthorized(Session session, String identifier, Permission permission);
}
