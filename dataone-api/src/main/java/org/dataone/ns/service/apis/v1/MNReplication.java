package org.dataone.ns.service.apis.v1;

import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Interface definition for the Tier 4 services.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">The DataONE Member Node
 *      specification</a>
 */
public interface MNReplication {

  /**
   * Called by a Coordinating Node to request that the Member Node create a copy of the specified object by retrieving
   * it from another Member Node and storing it locally so that it can be made accessible to the DataONE system.
   * <p>
   * A successful operation is indicated by a HTTP status of 200 on the response.
   * <p>
   * Failure of the operation MUST be indicated by returning an appropriate exception.
   * <p>
   * Access control for this method MUST be configured to allow calling by Coordinating Nodes.
   * 
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InsufficientResources if the system determines that resource are exhausted
   * @throws UnsupportedType if the supplied object type is not supported
   * @throws ServiceFailure if the system is unable to service the request
   * @throws NotImplemented if the operation is unsupported
   */
  boolean replicate(Session session, SystemMetadata sysmeta, String sourceNode);
}