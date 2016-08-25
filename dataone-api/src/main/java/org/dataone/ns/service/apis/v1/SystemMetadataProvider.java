package org.dataone.ns.service.apis.v1;

import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Implementations of this provide access to system metadata.
 * <p>
 * This exists in a separate interface to improve decoupling. One can write components knowing that they can access
 * system metadata without having to know about all complex services available. The authorization is one example of this
 * as system metadata informs authorization rules.
 */
public interface SystemMetadataProvider {

  /**
   * Describes the object identified by id by returning the associated system metadata object.
   * 
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotFound if the DataONE object is not present on this node
   * @throws ServiceFailure if the system is unable to service the request
   */
  SystemMetadata getSystemMetadata(Session session, Identifier identifier);
}
