package org.dataone.ns.service.apis.v1;

import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/**
 * Interface definition for the Tier 2 services.
 * 
 * @see <a
 *      href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html</a>
 */
public interface MNAuthorization {

  /**
   * Test if the client identified by the session is allowed to perform an operation at the stated permission level on
   * the specific object.
   * 
   * @return true if the user is authorized, otherwise throws an exception
   * @throws NotFound if the DataONE object is not present on this node
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws ServiceFailure if the system is unable to service the request
   * @throws NotImplemented if the operation is unsupported
   */
  boolean isAuthorized(Session session, Identifier pid, Permission action);
}
