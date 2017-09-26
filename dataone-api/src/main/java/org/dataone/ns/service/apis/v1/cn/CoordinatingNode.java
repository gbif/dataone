package org.dataone.ns.service.apis.v1.cn;

import java.util.Set;

import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
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

  // TODO: more exceptions
  NodeList listNodes() throws ServiceFailure;

  SystemMetadata getSystemMetadata(Identifier identifier) throws InvalidToken, NotImplemented, NotAuthorized, NotFound, ServiceFailure;

  boolean isNodeAuthorized(Subject targetNodeSubject, String pid);
}
