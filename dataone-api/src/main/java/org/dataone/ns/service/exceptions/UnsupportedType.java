package org.dataone.ns.service.exceptions;

/**
 * The information presented appears to be unsupported. This error might be encountered when attempting to register
 * unrecognized science metadata for example.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class UnsupportedType extends D1Exception {

  private static final long serialVersionUID = 8374844773479988385L;
  private static final int CODE = 400;

  public UnsupportedType(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public UnsupportedType(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
