package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The provided identifier conflicts with an existing identifier in the DataONE system.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class IdentifierNotUnique extends DataONEException {

  private static final long serialVersionUID = 6726392850227334395L;

  public IdentifierNotUnique(String message, String detailCode, String pid) {
    super(message, detailCode, pid);
  }

  public IdentifierNotUnique(String message, String detailCode, String pid, String nodeId) {
    super(message, detailCode, nodeId, pid); // note change of order
  }
}
