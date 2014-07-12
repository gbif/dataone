package org.dataone.ns.service.exceptions2;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents an exception that will be emitted from a DataONE node.
 * <p>
 * The exceptions documented in the DataONE specification are numerous in number and many methods are required to throw
 * all of them. In all cases, these exceptions are returned across the HTTP interface. As such it does not make for
 * elegant code to have a whole host of exception definitions, none of which will be actioned upon within this JVM.
 * <p>
 * This design takes a different approach, where a single exception is declared for components, which has enough
 * information to generate appropriate HTTP responses for exceptional circumstances.
 * <p>
 * This class inherits the mutability present in {@link Exception}, and therefore one mutable only with the stack trace
 * and cause.
 * 
 * @see https://mule1.dataone.org/ArchitectureDocs-current/apis/Exceptions.html
 */
@ThreadSafe
public final class DataONEException extends Exception {

  public enum Type {
    SERVICE_FAILURE, NOT_FOUND
  }

  private static final long serialVersionUID = 6633336141436940865L;;

  private final Type type;
  private final String detailCode;
  @Nullable
  private final String nodeId;
  @Nullable
  private final String pid;

  private DataONEException(Type type, String message, String detailCode) {
    super(message);
    this.type = type;
    this.detailCode = detailCode;
    this.nodeId = null;
    this.pid = null;
  }

  private DataONEException(Type type, String message, String detailCode, String nodeId) {
    super(message);
    this.type = type;
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = null;
  }

  private DataONEException(Type type, String message, String detailCode, String nodeId, String pid) {
    super(message);
    this.type = type;
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = pid;
  }

  private DataONEException(Type type, String message, String detailCode, String nodeId, String pid, Throwable cause) {
    super(message, cause);
    this.type = type;
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = pid;
  }

  private DataONEException(Type type, String message, String detailCode, String nodeId, Throwable cause) {
    super(message, cause);
    this.type = type;
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = null;
  }

  private DataONEException(Type type, String message, String detailCode, Throwable cause) {
    super(message, cause);
    this.type = type;
    this.detailCode = detailCode;
    this.nodeId = null;
    this.pid = null;
  }

  public String getDetailCode() {
    return detailCode;
  }

  public String getPid() {
    return pid;
  }

  public Type getType() {
    return type;
  }

  public DataONEException notFound(String message, String detailCode, String pid) {
    return new DataONEException(Type.NOT_FOUND, message, detailCode, pid);
  }

  public DataONEException notFound(String message, String detailCode, String pid, Throwable cause) {
    return new DataONEException(Type.NOT_FOUND, message, detailCode, pid, cause);
  }

  public DataONEException serviceFailure(String message, String detailCode, Exception cause) {
    return new DataONEException(Type.SERVICE_FAILURE, message, detailCode, cause);
  }

  public DataONEException serviceFailure(String message, String detailCode, Throwable cause) {
    return new DataONEException(Type.SERVICE_FAILURE, message, detailCode, cause);
  }

}