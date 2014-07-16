package org.dataone.ns.service.apis.v1;

import java.util.Date;

import org.dataone.ns.service.exceptions.InvalidRequest;
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
public interface MemberNodeAuthorization {

  /**
   * Test if the client identified by the session is allowed to perform an operation at the stated permission level on
   * the specific object.
   * 
   * @return true if the user is authorized, otherwise throws an exception
   * @throws NotFound If identified object does not exist on this node
   * @throws NotAuthorized If the subject identified in the session does not have permission
   * @throws NotImplemented If the implementation does not support the operation
   * @throws InvalidRequest If any argument is null
   * @throws ServiceFailure If the backend systems cannot be read, or the coordinating node cannot be accessed
   */
  boolean isAuthorized(Session session, String pid, Permission action);

  /**
   * Notifies the Member Node that the authoritative copy of system metadata on the Coordinating Nodes has changed.
   * <p>
   * The implementation should schedule an update to its information about the affected object by retrieving an
   * authoritative copy from a Coordinating Node. This can be accepted and return immediately provided the
   * implementation intends to perform the operation under normal circumstances.
   * 
   * @return true if the request was accepted
   * @throws NotFound If identified object does not exist on this node
   * @throws NotAuthorized If the subject identified in the session does not have permission
   * @throws NotImplemented If the implementation does not support the operation
   * @throws InvalidRequest If any argument is null
   * @throws ServiceFailure If the backend systems cannot be read, or the coordinating node cannot be accessed
   */
  boolean
    systemMetadataChanged(Session session, Identifier pid, long serialVersion, Date dateSystemMetadataLastModified);
}
