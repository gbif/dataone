package org.dataone.ns.service.exceptions;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.base.Objects;

/**
 * The container object suitable for serializing exceptions over the wire.
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExceptionDetail {

  @XmlAttribute(name = "name")
  protected String name;

  @XmlAttribute(name = "errorCode")
  protected int errorCode;

  @XmlAttribute(name = "detailCode")
  protected String detailCode;

  @XmlAttribute(name = "nodeId")
  protected String nodeId;

  @XmlAttribute(name = "pid")
  @Nullable
  protected String pid;

  @XmlValue
  protected String description;

  public ExceptionDetail(String name, int errorCode, String detailCode, String description, String nodeId, String pid) {
    this.name = name;
    this.errorCode = errorCode;
    this.detailCode = detailCode;
    this.description = description;
    this.nodeId = nodeId;
    this.pid = pid;
  }

  /**
   * Required for simplifying JAXB - not intended for developer use and might be removed.
   */
  protected ExceptionDetail() {
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("name", name)
      .add("errorCode", errorCode)
      .add("detailCode", detailCode)
      .add("nodeId", nodeId)
      .add("pid", pid)
      .add("description", description)
      .toString();
  }
}
