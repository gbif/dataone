package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Some sort of system failure occurred that is preventing the requested operation from completing successfully. This
 * error can be raised by any method in the DataONE API.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class ServiceFailure extends DataONEException {

  private static final long serialVersionUID = -4044845581507746254L;

  public ServiceFailure(String message) {
    super(message);
  }

  public ServiceFailure(String message, String detailCode) {
    super(message, detailCode, null, null);
  }

  public ServiceFailure(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId, null);
  }

  public ServiceFailure(String message, String detailCode, String nodeId, Throwable cause) {
    super(message, detailCode, nodeId, null, cause);
  }

  public ServiceFailure(String message, String detailCode, Throwable cause) {
    super(message, detailCode, null, null, cause);
  }

  public ServiceFailure(String message, Throwable cause) {
    super(message, cause);
  }
}
