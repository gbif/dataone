package org.dataone.ns.service.exceptions;

/**
 * The science metadata document submitted is not of a type that is recognized by the DataONE system.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
public class UnsupportedMetadataType extends D1Exception {

  private static final long serialVersionUID = 4661374954195073838L;
  private static final int CODE = 400;

  public UnsupportedMetadataType(String message, String detailCode) {
    super(CODE, message, detailCode);
  }

  public UnsupportedMetadataType(String message, String detailCode, String pid, String nodeId) {
    super(CODE, message, detailCode, pid, nodeId);
  }
}
