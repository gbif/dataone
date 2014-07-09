package org.dataone.ns.service.exceptions;

import org.dataone.ns.service.types.v1.Session;

/**
 * The supplied authentication token ({@link Session}) could not be verified as being valid.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class InvalidToken extends D1Exception {

  private static final long serialVersionUID = -7325891741882641232L;
  private static final int CODE = 401;

  public InvalidToken(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public InvalidToken(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
