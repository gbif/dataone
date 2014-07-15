package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

import org.dataone.ns.service.types.v1.Session;

/**
 * The supplied authentication token ({@link Session}) could not be verified as being valid.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class InvalidToken extends DataONEException {

  private static final long serialVersionUID = -7325891741882641232L;

  public InvalidToken(String message) {
    super(message);
  }

  public InvalidToken(String message, String detailCode) {
    super(message, detailCode, null, null);
  }

  public InvalidToken(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId, null);
  }
}
