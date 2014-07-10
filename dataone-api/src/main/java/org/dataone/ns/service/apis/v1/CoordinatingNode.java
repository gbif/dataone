package org.dataone.ns.service.apis.v1;

import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.NodeList;

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
}
