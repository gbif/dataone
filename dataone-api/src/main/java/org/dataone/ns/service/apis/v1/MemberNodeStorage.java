package org.dataone.ns.service.apis.v1;

import java.io.InputStream;

import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/**
 * Interface definition for the Tier 3 services.
 * TODO: Used String for Identifier; consider using Identifier instead
 * 
 * @see <a
 *      href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html</a>
 */
public interface MemberNodeStorage extends MemberNodeAuthorization {

  /**
   * Hides an object managed by DataONE from search operations, effectively preventing its discovery during normal
   * operations.
   * <p>
   * The operation does not delete the object bytes, but instead sets the Types.SystemMetadata.archived flag to True.
   * This ensures that the object can still be resolved (and hence remain valid for existing citations and cross
   * references), though will not appear in searches.
   * <p>
   * Objects that are archived can not be updated through the MNStorage.update() operation.
   * <p>
   * Archived objects can not be un-archived. This behavior may change in future versions of the DataONE API.
   * <p>
   * Member Nodes MUST check that the caller is authorized to perform this function. If the object does not exist on the
   * node servicing the request, then an Exceptions.NotFound exception is raised. The message body of the exception
   * SHOULD contain a hint as to the location of the CNRead.resolve() method.
   */
  Identifier archive(Session session, String pid) throws InvalidToken, ServiceFailure,
    NotAuthorized, NotFound, NotImplemented;

  /**
   * Called by a client to adds a new object to the Member Node.
   */
  Identifier create(Session session, String pid, InputStream object,
    SystemMetadata sysmeta) throws IdentifierNotUnique, InsufficientResources,
    InvalidRequest, InvalidSystemMetadata, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure, UnsupportedType;

  /**
   * Deletes an object managed by DataONE from the Member Node. Member Nodes MUST check that the caller (typically a
   * Coordinating Node) is authorized to perform this function.
   * <p>
   * The delete operation will be used primarily by Coordinating Nodes to help manage the number of replicas of an
   * object that are present in the entire system.
   * <p>
   * The operation removes the object from further interaction with DataONE services. The implementation may delete the
   * object bytes, and in general should do so since a delete operation may be in response to a problem with the object
   * (e.g. it contains malicious content, is inappropriate, or is the subject of a legal request).
   * <p>
   * If the object does not exist on the node servicing the request, then an Exceptions.NotFound exception is raised.
   * The message body of the exception SHOULD contain a hint as to the location of the CNRead.resolve() method.
   */
  Identifier delete(Session session, String pid) throws InvalidToken, ServiceFailure, NotAuthorized, NotFound,
    NotImplemented;

  /**
   * Given a scheme and optional fragment, generates an identifier with that scheme and fragment that is unique.
   */
  Identifier generateIdentifier(Session session, String scheme, String fragment) throws InvalidToken, ServiceFailure,
    NotAuthorized, NotImplemented, InvalidRequest;

  /**
   * This method is called by clients to update objects on Member Nodes.
   * <p>
   * Updates an existing object by creating a new object identified by newPid on the Member Node which explicitly
   * obsoletes the object identified by pid through appropriate changes to the SystemMetadata of pid and newPid.
   * <p>
   * The Member Node sets Types.SystemMetadata.obsoletedBy on the object being obsoleted to the pid of the new object.
   * It then updates Types.SystemMetadata.dateSysMetadataModified on both the new and old objects. The modified system
   * metadata entries then become available in MNRead.listObjects(). This ensures that a Coordinating Node will pick up
   * the changes when filtering on Types.SystemMetadata.dateSysMetadataModified.
   * <p>
   * The update operation MUST fail with Exceptions.InvalidRequest on objects that have the
   * Types.SystemMetadata.archived property set to true.
   * <p>
   * A new, unique Types.SystemMetadata.seriesId may be included when beginning a series, or a series may be extended if
   * the newPid obsoletes the existing pid.
   */
  Identifier update(Session session, String pid, InputStream object, String newPid, SystemMetadata sysmeta)
    throws IdentifierNotUnique,
    InsufficientResources, InvalidRequest, InvalidSystemMetadata, InvalidToken, NotAuthorized, NotImplemented,
    ServiceFailure, UnsupportedType, NotFound;

}