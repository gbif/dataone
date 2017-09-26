package org.gbif.d1.mn.auth;

import javax.servlet.http.HttpServletRequest;

import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SystemMetadata;

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

  /**
   * Have the credentials of this request a specific permission on the object represented by this identifier?
   */
  Session checkIsAuthorized(HttpServletRequest request, String identifier, Permission permission);

  /**
   * Have the credentials of this request a specific permission?
   */
  Session checkIsAuthorized(HttpServletRequest request, Permission permission);

  /**
   * Has this session a specific permission?
   */
  Session checkIsAuthorized(Session session, Permission permission);

  /**
   * Has the session a particular permission on a object represented by this identifier?
   */
  Session checkIsAuthorized(Session session, String identifier, Permission permission);

  /**
   * Has the session a particular permission on a object represented by this metadata?
   */
  boolean checkIsAuthorized(Session session, SystemMetadata sysMetadata, Permission permission);

  /**
   * Is the subject coming from an Authority Node or a Coordinating Node where the metadata was issued.
   */
  boolean isAuthorityNodeOrCN(String subject, SystemMetadata sysMetadata);

  /**
   * Is this subject a Coordinating Node subject?
   */
  boolean isCNNode(String subject);

  /**
   * Can the subject execute a DataONE Method?
   */
  boolean isAuthorized(String method, Subject subject);
}
