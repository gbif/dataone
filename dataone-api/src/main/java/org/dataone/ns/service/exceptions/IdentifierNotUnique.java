package org.dataone.ns.service.exceptions;

/**
 * The provided identifier conflicts with an existing identifier in the DataONE system. When serializing, the identifier
 * in conflict should be rendered in traceInformation as the value of an identifier key.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class IdentifierNotUnique extends D1Exception {

  private static final long serialVersionUID = 6726392850227334395L;
  private static final int CODE = 409;

  public IdentifierNotUnique(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public IdentifierNotUnique(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
