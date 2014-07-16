package org.gbif.d1.mn.backend;

import com.google.common.base.Preconditions;

/**
 * An instance of this class should describe the health of a backend system containing an exception to encapsulate the
 * issue should it not be considered healthy.
 * <p>
 * This class is conditionally thread-safe. It is unconditionally thread-safe if constructed with a thread-safe
 * Exception which should be normal practice.
 */
public class Health {

  private final Exception cause;
  private final boolean healthy;

  private Health(boolean healthy, Exception cause) {
    this.healthy = healthy;
    this.cause = cause;
  }

  public static Health healthy() {
    return new Health(true, null);
  }

  public static Health unhealthy(Exception cause) {
    Preconditions.checkNotNull(cause, "The cause of the reporting unhealth is required"); // invalid use
    return new Health(false, cause);
  }

  /**
   * @return The exception which will always null when healthy and populated when unhealthy
   */
  public Exception getCause() {
    return cause;
  }

  public boolean isHealthy() {
    return healthy;
  }
}
