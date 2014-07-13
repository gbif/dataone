package org.gbif.d1.mn.auth;

import javax.servlet.http.HttpServletRequest;

import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;

/**
 * The interface to objects that are able to enforce DataONE authorization rules.
 * Implementations of this class must be unconditionally thread-safe and document as such.
 */
public interface AuthorizationManager {

  /**
   * The OID for DataONE extensions which was registered with cilogon.org.
   */
  String DEFAULT_OID_SUBJECT_INFO = "1.3.6.1.4.1.34998.2.1";

  /**
   * Verifies that the given request is authorized to perform the given permission on the identified object. If the
   * request should not be authorized then an exception is thrown.
   * <p>
   * The high level procedure for this is to extract all the subjects from the certificate presented with the request
   * and then succeed if any of the following conditions are met.
   * <ul>
   * <li>The object has an explicit access rule granting permission</li>
   * <li>The request originates from rights holder of the object</li>
   * <li>The request originates from the same server that is servicing the request</li>
   * <li>The request originates from known coordinating node</li>
   * </ul>
   * <p>
   * Note that some of these requests require a callback to the coordinating nodes in the DataONE network.
   * 
   * @param request Which must have a certificate
   * @param identifier Of the object the caller wishes to access
   * @param permission The level of access sought
   * @param detailCode The detail code to pass into the exception if one is being raised
   * @throws NotAuthorized If the checks ran to completion and the caller is not authorized
   * @throws NotFound If the identified object is not found on this node
   * @throws ServiceFailure If an error occurs, including connecting to a coordinating node
   * @throws InvalidToken If a DataONE specific extension in the certificate was not readable or the request malformed
   * @throws InsufficientResources If the implementation decides it is refusing access due to a resource limit
   * @throws InvalidRequest If the request is not well formed for any reason other than an InvalidToken
   */
  void checkIsAuthorized(HttpServletRequest request, Identifier identifier, Permission permission, String detailCode);
}
