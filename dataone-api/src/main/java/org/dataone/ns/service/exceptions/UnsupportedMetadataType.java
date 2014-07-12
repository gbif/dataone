package org.dataone.ns.service.exceptions;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The science metadata document submitted is not of a type that is recognized by the DataONE system.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html">
 *      https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html</a>
 */
@ThreadSafe
public class UnsupportedMetadataType extends DataONEException {

  private static final long serialVersionUID = 4661374954195073838L;

  public UnsupportedMetadataType(String message, String detailCode) {
    super(message, detailCode);
  }

  public UnsupportedMetadataType(String message, String detailCode, String nodeId) {
    super(message, detailCode, nodeId);
  }
}
