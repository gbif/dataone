package org.dataone.ns.service.apis.v1;

import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.NodeList;

/**
 * TODO: Move this to some common
 * It is a <strong>requirement</strong> that implementations of this be thread-safe.
 */
public interface CoordinatingNode {

  NodeList listNodes() throws ServiceFailure;
}
