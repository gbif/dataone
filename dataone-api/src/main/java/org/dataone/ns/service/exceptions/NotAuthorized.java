package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The supplied identity information is not authorized for the requested operation.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class NotAuthorized extends DataONEException {

  private static final long serialVersionUID = -4069266622213984585L;

  public NotAuthorized(String message, String detailCode) {
    super(message, detailCode);
  }

  public NotAuthorized(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId);
  }
}
