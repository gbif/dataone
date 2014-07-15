package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Indicates that the credentials supplied are invalid for some reason.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class InvalidCredentials extends DataONEException {

  private static final long serialVersionUID = -2431090833531104825L;

  public InvalidCredentials(String message) {
    super(message);
  }

  public InvalidCredentials(String message, String detailCode) {
    super(message, detailCode, null, null);
  }

  public InvalidCredentials(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId, null);
  }
}
