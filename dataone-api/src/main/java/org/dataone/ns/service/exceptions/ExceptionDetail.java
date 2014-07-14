package org.dataone.ns.service.exceptions;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The container object suitable for serializing exceptions over the wire.
 */
@XmlRootElement(name = "error")
@Immutable
public class ExceptionDetail {

  @XmlAttribute(name = "name")
  private final String name;

  @XmlAttribute(name = "errorCode")
  private final String errorCode;

  @XmlAttribute(name = "detailCode")
  private final String detailCode;

  @XmlAttribute(name = "nodeId")
  private final String nodeId;

  @XmlAttribute(name = "pid")
  @Nullable
  private final String pid;

  @XmlValue
  private final String description;

  public ExceptionDetail(String name, String errorCode, String detailCode, String description, String nodeId, String pid) {
    this.name = name;
    this.errorCode = errorCode;
    this.detailCode = detailCode;
    this.description = description;
    this.nodeId = nodeId;
    this.pid = pid;
  }
}
