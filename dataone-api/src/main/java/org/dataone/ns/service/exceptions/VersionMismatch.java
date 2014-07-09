package org.dataone.ns.service.exceptions;

/**
 * The serialVersion of the system metadata being updated differs from the serialVersion supplied with the change
 * request.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class VersionMismatch extends D1Exception {

  private static final long serialVersionUID = 6015751043767336291L;
  private static final int CODE = 409;

  public VersionMismatch(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public VersionMismatch(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
