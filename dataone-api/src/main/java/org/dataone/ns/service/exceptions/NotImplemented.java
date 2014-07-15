package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A method is not implemented, or alternatively, features of a particular method are not implemented.
 * <p>
 * Note: this class exists because it is in the DataONE specification documented as an exception with detailCode etc.
 * Thus the more familiar {@link UnsupportedOperationException} cannot be used.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class NotImplemented extends DataONEException {

  private static final long serialVersionUID = 8904494284521053331L;

  public NotImplemented(String message) {
    super(message);
  }

  public NotImplemented(String message, String detailCode) {
    super(message, detailCode, null, null);
  }

  public NotImplemented(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId, null);
  }
}
