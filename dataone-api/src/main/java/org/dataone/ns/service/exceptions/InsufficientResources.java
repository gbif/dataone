package org.dataone.ns.service.exceptions;

/**
 * There are insufficient resources at the node to support the requested operation.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class InsufficientResources extends D1Exception {

  private static final long serialVersionUID = 1521685050288847713L;
  private static final int CODE = 413;

  public InsufficientResources(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public InsufficientResources(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
