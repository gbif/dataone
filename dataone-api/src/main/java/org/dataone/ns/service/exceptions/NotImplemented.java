package org.dataone.ns.service.exceptions;

/**
 * A method is not implemented, or alternatively, features of a particular method are not implemented.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class NotImplemented extends D1Exception {

  private static final long serialVersionUID = 8904494284521053331L;
  private static final int CODE = 501;

  public NotImplemented(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public NotImplemented(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
