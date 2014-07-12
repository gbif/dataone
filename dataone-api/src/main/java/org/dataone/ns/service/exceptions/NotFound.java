package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Used to indicate that an object is not present on the node where the exception was raised.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class NotFound extends DataONEException {

  private static final long serialVersionUID = -8776645677598300180L;

  public NotFound(String message, String detailCode, String pid) {
    super(message, detailCode, pid);
  }

  public NotFound(String message, String detailCode, String pid, String nodeId) {
    super(message, detailCode, nodeId, pid); // note change of order
  }
}
