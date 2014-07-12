package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The supplied system metadata is invalid. This could be because some required field is not set, the metadata document
 * is malformed, or the value of some field is not valid.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class InvalidSystemMetadata extends DataONEException {

  private static final long serialVersionUID = -5840884345956489739L;

  public InvalidSystemMetadata(String message, String detailCode) {
    super(message, detailCode);
  }

  public InvalidSystemMetadata(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId);
  }
}
