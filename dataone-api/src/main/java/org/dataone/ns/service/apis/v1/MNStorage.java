package org.dataone.ns.service.apis.v1;

import java.io.InputStream;

import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Interface definition for the Tier 3 services.
 * <p>
 * All methods can throw:
 * <ul>
 * <li>{@link NotAuthorized} if the credentials presented do not have permission to perform the action</li>
 * <li>{@link InvalidToken} if the credentials in the request are not correctly presented</li>
 * <li>{@link ServiceFailure} if the system is unable to service the request</li>
 * <li>{@link NotImplemented} if the operation is unsupported</li>
 * </ul>
 * Implementations are encouraged <strong>not</strong> to throw other runtime exceptions.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">The DataONE Member Node
 *      specification</a>
 */
public interface MNStorage {

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
   * Member Nodes MUST check that the caller is authorized to perform this function. if the object does not exist on the
   * node servicing the request, then an Exceptions.NotFound exception is raised. The message body of the exception
   * SHOULD contain a hint as to the location of the CNRead.resolve() method.
   * 
   * @throws NotFound if the DataONE object is not present on this node
   */
  Identifier archive(Session session, Identifier pid);

  /**
   * Called by a client to adds a new object to the Member Node.
   * 
   * @throws IdentifierNotUnique if the identifier already exists within DataONE
   * @throws InsufficientResources if the system determines that resource are exhausted
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InvalidSystemMetadata if the system metadata is not well formed
   * @throws UnsupportedType if the supplied object type is not supported
   */
  Identifier create(Session session, Identifier pid, InputStream object, SystemMetadata sysmeta);

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
   * if the object does not exist on the node servicing the request, then an Exceptions.NotFound exception is raised.
   * The message body of the exception SHOULD contain a hint as to the location of the CNRead.resolve() method.
   * 
   * @throws NotFound if the DataONE object is not present on this node
   */
  Identifier delete(Session session, Identifier pid);

  /**
   * Given a scheme and optional fragment, generates an identifier with that scheme and fragment that is unique.
   * 
   * @throws InvalidRequest if any argument is null or fails validation
   */
  Identifier generateIdentifier(Session session, String scheme, String fragment);

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
   * 
   * @throws IdentifierNotUnique if the identifier already exists within DataONE
   * @throws InsufficientResources if the system determines that resource are exhausted
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InvalidSystemMetadata if the system metadata is not well formed
   * @throws UnsupportedType if the supplied object type is not supported
   * @throws NotFound if the DataONE object is not present on this node
   */
  Identifier update(Session session, Identifier pid, InputStream object, Identifier newPid, SystemMetadata sysmeta);

}