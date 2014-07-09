package org.dataone.ns.service.exceptions;

/**
 * The authentication request timed out.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class AuthenticationTimeout extends D1Exception {

  private static final long serialVersionUID = -6329869270435775319L;
  private static final int CODE = 408;

  public AuthenticationTimeout(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public AuthenticationTimeout(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
