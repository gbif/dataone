package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The information presented appears to be unsupported. This error might be encountered when attempting to register
 * unrecognized science metadata for example.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class UnsupportedType extends DataONEException {

  private static final long serialVersionUID = 8374844773479988385L;

  public UnsupportedType(String message) {
    super(message);
  }

  public UnsupportedType(String message, String detailCode) {
    super(message, detailCode, null, null);
  }

  public UnsupportedType(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId, null);
  }
}
