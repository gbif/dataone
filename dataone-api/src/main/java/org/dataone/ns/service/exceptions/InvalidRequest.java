package org.dataone.ns.service.exceptions;

/**
 * The parameters provided in the call were invalid.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class InvalidRequest extends D1Exception {

  private static final long serialVersionUID = -8453320521331323991L;
  private static final int CODE = 400;

  public InvalidRequest(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public InvalidRequest(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
