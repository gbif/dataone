package org.dataone.ns.service.apis.v1.cn;

import java.util.Set;

import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeList;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Provides the interface that the coordinating nodes of the DataONE network offer.
 * <p>
 * Note: currently only supports those calls required to implement a MemberNode.
 * <p>
 * It is a <strong>requirement</strong> that implementations of this be thread-safe and document as such.
 *
 * @see <a
 *      href="http://mule1.dataone.org/ArchitectureDocs-current/apis/CN_APIs.html">http://mule1.dataone.org/ArchitectureDocs-current/apis/CN_APIs.html</a>
 */
public interface CoordinatingNode {

  /**
   * Returns a list of nodes that have been registered with the DataONE infrastructure.
   */
  NodeList listNodes() throws ServiceFailure;

  /**
   * Returns the system metadata that contains DataONE specific information about the object identified by id.
   * Authoritative copies of system metadata are only available from the Coordinating Nodes.
   */
  SystemMetadata getSystemMetadata(Identifier identifier) throws InvalidToken, NotImplemented, NotAuthorized, NotFound, ServiceFailure;

  /**
   * Verifies that a replication event was initiated by a CN by comparing the target node’s identifiying subject with a
   * known list of scheduled replication tasks.
   */
  boolean isNodeAuthorized(Subject targetNodeSubject, String pid);

  /**
   * For retrieving the capabilities of the specified node if it is registered on the Coordinating Node being called.
   */
  Node getNodeCapabilities(String nodeId);
}
