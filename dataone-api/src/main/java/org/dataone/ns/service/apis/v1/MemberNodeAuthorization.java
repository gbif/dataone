package org.dataone.ns.service.apis.v1;

import java.util.Date;

import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/**
 * Interface definition for the Tier 2 services.
 * TODO: Used String for Identifier; consider using Identifier instead
 * 
 * @see <a
 *      href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html</a>
 */
public interface MemberNodeAuthorization extends MemberNodeRead {

  /**
   * Test if the user identified by the provided session has authorization for operation on the specified object.
   */
  boolean isAuthorized(String pid, Permission action) throws ServiceFailure, InvalidRequest, InvalidToken, NotFound,
    NotAuthorized, NotImplemented;

  /**
   * Notifies the Member Node that the authoritative copy of system metadata on the Coordinating Nodes has changed.
   */
  boolean systemMetadataChanged(Identifier pid, long serialVersion, Date dateSystemMetadataLastModified)
    throws InvalidToken, ServiceFailure, NotAuthorized, NotImplemented, InvalidRequest;

}