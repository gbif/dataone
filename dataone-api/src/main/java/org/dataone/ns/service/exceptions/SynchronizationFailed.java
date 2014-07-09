package org.dataone.ns.service.exceptions;

/**
 * Sent to a Member Node from a Coordinating Node when an attempt to synchronize some object fails.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class SynchronizationFailed extends D1Exception {

  private static final long serialVersionUID = 3976666727186052060L;
  private static final int CODE = 0;

  public SynchronizationFailed(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public SynchronizationFailed(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
