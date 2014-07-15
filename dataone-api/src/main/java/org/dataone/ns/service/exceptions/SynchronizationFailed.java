package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Sent to a Member Node from a Coordinating Node when an attempt to synchronize some object fails.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class SynchronizationFailed extends DataONEException {

  private static final long serialVersionUID = 3976666727186052060L;

  public SynchronizationFailed(String message, String pid) {
    super(message, null, null, pid); // careful to call correct one
  }

  public SynchronizationFailed(String message, String detailCode, String pid) {
    super(message, detailCode, null, pid);
  }

  public SynchronizationFailed(String message, String detailCode, String pid, String nodeId) {
    super(message, detailCode, nodeId, pid); // note change of order
  }

  public SynchronizationFailed(String message, String detailCode, String pid, String nodeId, Throwable cause) {
    super(message, detailCode, nodeId, pid, cause); // note change of order of pid and nodeId
  }

  public SynchronizationFailed(String message, String detailCode, String pid, Throwable cause) {
    super(message, detailCode, null, pid, cause);
  }

  public SynchronizationFailed(String message, String pid, Throwable cause) {
    super(message, null, null, pid, cause); // careful to call correct one
  }
}
