package org.dataone.ns.service.exceptions;

/**
 * Some sort of system failure occurred that is preventing the requested operation from completing successfully. This
 * error can be raised by any method in the DataONE API.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class ServiceFailure extends D1Exception {

  private static final long serialVersionUID = -4044845581507746254L;
  private static final int CODE = 500;

  public ServiceFailure(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public ServiceFailure(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
