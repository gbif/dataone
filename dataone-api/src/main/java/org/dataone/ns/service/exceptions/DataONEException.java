package org.dataone.ns.service.exceptions;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Objects;

/**
 * Represents an exception that will be emitted from a DataONE node.
 * <p>
 * The exceptions documented in the DataONE Exceptions guide are numberous in number and many methods are required to
 * throw all of them. In all cases, these exceptions are thrown across the HTTP interface. As such it does not make for
 * elegant code to have a whole host of exception definitions, none of which will be actioned upon within this JVM.
 * <p>
 * This design takes a different approach, where a single exception is declared for components, which has enough
 * information to generate appropriate HTTP responses for exceptional circumstances.
 * <p>
 * This class inherits the mutability present in {@link Exception}, and thus is mutable only on the stack trace and
 * cause.
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
  private final String message;
  private final String detailCode;
  @Nullable
  private final String pid;
  @Nullable
  private final Exception cause;

  // not for instantiation
  private DataONEException(Type type, String message, String detailCode, String pid, Exception cause) {
    super();
    this.type = type;
    this.message = message;
    this.detailCode = detailCode;
    this.pid = pid;
    this.cause = cause;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof DataONEException) {
      if (!super.equals(object)) {
        return false;
      }
      DataONEException that = (DataONEException) object;
      return Objects.equal(this.type, that.type)
        && Objects.equal(this.message, that.message)
        && Objects.equal(this.detailCode, that.detailCode)
        && Objects.equal(this.pid, that.pid)
        && Objects.equal(this.cause, that.cause);
    }
    return false;
  }

  @Override
  public Exception getCause() {
    return cause;
  }

  public String getDetailCode() {
    return detailCode;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public String getPid() {
    return pid;
  }

  public Type getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), type, message, detailCode, pid, cause);
  }

  public DataONEException newNotFoundException(Type type, String message, String detailCode, String pid) {
    return new DataONEException(type, message, detailCode, pid, null);
  }

  public DataONEException newServiceFailureException(Type type, String message, String detailCode) {
    return new DataONEException(type, message, detailCode, null, null);
  }

  public DataONEException newServiceFailureException(Type type, String message, String detailCode, Exception cause) {
    return new DataONEException(type, message, detailCode, null, cause);
  }

  public DataONEException newServiceFailureException(Type type, String message, String detailCode, String pid,
    Exception cause) {
    DataONEException e = new DataONEException(type, message, detailCode, pid, cause);
    return new DataONEException(type, message, detailCode, pid, cause);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("type", type)
      .add("message", message)
      .add("detailCode", detailCode)
      .add("pid", pid)
      .add("cause", cause)
      .toString();
  }
}
