package org.gbif.d1.mn.auth;

import javax.servlet.http.HttpServletRequest;

import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidCredentials;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;

/**
 * The interface to objects that are able to enforce DataONE authorization rules.
 * Implementations of this class should target thread-safety, and document as such.
 */
public interface AuthorizationManager {

  public static final String DEFAULT_OID_SUBJECT_INFO = "1.3.6.1.4.1.34998.2.1";

  /**
   * TODO: document
   * Extracts the certificate from the request, which must be present and runs the authentication routine.
   * This will either complete indicating that the subject is authorized or a NotAuthorized exception will be thrown.
   * 
   * @throws InsufficientResources
   * @throws NotImplemented
   * @throws InvalidToken
   * @throws ServiceFailure
   * @throws NotFound
   * @throws NotAuthorized
   * @throws InvalidCredentials
   * @throws InvalidRequest
   */
  void checkIsAuthorized(HttpServletRequest request, Identifier identifier, Permission permission,
    String detailCode) throws NotAuthorized, NotFound, ServiceFailure, InvalidToken, NotImplemented,
    InsufficientResources, InvalidRequest, InvalidCredentials;

}
