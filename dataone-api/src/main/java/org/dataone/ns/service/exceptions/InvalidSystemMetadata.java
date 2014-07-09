package org.dataone.ns.service.exceptions;

/**
 * The supplied system metadata is invalid. This could be because some required field is not set, the metadata document
 * is malformed, or the value of some field is not valid.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class InvalidSystemMetadata extends D1Exception {

  private static final long serialVersionUID = -5840884345956489739L;
  private static final int CODE = 400;

  public InvalidSystemMetadata(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public InvalidSystemMetadata(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
