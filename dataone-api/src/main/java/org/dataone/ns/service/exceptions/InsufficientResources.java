package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * There are insufficient resources at the node to support the requested operation.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class InsufficientResources extends DataONEException {

  private static final long serialVersionUID = 1521685050288847713L;

  public InsufficientResources(String message) {
    super(message);
  }

  public InsufficientResources(String message, String detailCode) {
    super(message, detailCode, null, null);
  }

  public InsufficientResources(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId, null);
  }
}
