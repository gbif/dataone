package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The serialVersion of the system metadata being updated differs from the serialVersion supplied with the change
 * request.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class VersionMismatch extends DataONEException {

  private static final long serialVersionUID = 6015751043767336291L;

  public VersionMismatch(String message, String detailCode) {
    super(message, detailCode);
  }

  public VersionMismatch(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId);
  }
}
