package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The authentication request timed out.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class AuthenticationTimeout extends DataONEException {

  private static final long serialVersionUID = -6329869270435775319L;

  public AuthenticationTimeout(String message) {
    super(message);
  }

  public AuthenticationTimeout(String message, String detailCode) {
    super(message, detailCode, null, null);
  }

  public AuthenticationTimeout(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId, null);
  }
}
