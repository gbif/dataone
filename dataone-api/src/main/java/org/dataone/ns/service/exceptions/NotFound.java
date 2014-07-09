package org.dataone.ns.service.exceptions;

/**
 * Used to indicate that an object is not present on the node where the exception was raised. The error message should
 * include a reference to the CN_crud.resolve() method URL for the object.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class NotFound extends D1Exception {

  private static final long serialVersionUID = -8776645677598300180L;
  private static final int CODE = 404;

  public NotFound(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public NotFound(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
