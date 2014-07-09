package org.dataone.ns.service.exceptions;

/**
 * The supplied identity information is not authorized for the requested operation.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class NotAuthorized extends D1Exception {

  private static final long serialVersionUID = -4069266622213984585L;
  private static final int CODE = 401;

  public NotAuthorized(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public NotAuthorized(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
