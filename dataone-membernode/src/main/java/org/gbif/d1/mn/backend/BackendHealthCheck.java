package org.gbif.d1.mn.backend;

import com.codahale.metrics.health.HealthCheck;

/**
 * A simple bridge connecting a health check from the backend into the DropWizard framework allowing it to be
 * monitored. This class only exists to ensure that backend implementations do not need to depend on DropWizard code.
 */
public class BackendHealthCheck extends HealthCheck {

  private final MNBackend backend;

  public BackendHealthCheck(MNBackend backend) {
    this.backend = backend;
  }

  @Override
  protected Result check() throws Exception {
    Health h = backend.health();
    if (h.isHealthy()) {
      return Result.healthy();
    } else {
      return Result.unhealthy(h.getCause());
    }
  }
}