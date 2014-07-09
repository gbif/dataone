package org.dataone.ns.service.exceptions;

/**
 * Indicates that the credentials supplied are invalid for some reason.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class InvalidCredentials extends D1Exception {

  private static final long serialVersionUID = -2431090833531104825L;
  private static final int CODE = 401;

  public InvalidCredentials(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public InvalidCredentials(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
