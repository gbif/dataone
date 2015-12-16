package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.exception.DataONE;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operations relating to basic status monitoring such as a Ping request.
 * <p>
 * All methods can throw:
 * <ul>
 * <li>{@link NotAuthorized} if the credentials presented do not have permission to perform the action</li>
 * <li>{@link InvalidToken} if the credentials in the request are not correctly presented</li>
 * <li>{@link ServiceFailure} if the system is unable to service the request</li>
 * <li>{@link NotImplemented} if the operation is unsupported</li>
 * </ul>
 */
@Path("/mn/v1/monitor")
@Singleton
public final class MonitorResource {
  private static final Logger LOG = LoggerFactory.getLogger(MonitorResource.class);
  private static final String DATE_FORMAT = "HH:mm:ss Z 'on' EEE, MMM d, yyyy";
  @VisibleForTesting
  static final DateTimeFormatter DTF = DateTimeFormat.forPattern(DATE_FORMAT); // threadsafe

  private final MNBackend backend;

  public MonitorResource(MNBackend backend) {
    Preconditions.checkNotNull(backend, "A backend is required");
    this.backend = backend;
  }

  /**
   * Returns a human readable form of the time for easy debugging since the specification is ambiguous.
   *
   * @throws InsufficientResources if the system determines that resource are exhausted
   */
  @GET
  @Path("ping")
  @Produces(MediaType.TEXT_PLAIN) // the specification does not dictate a format, so opt for readability
  @DataONE(DataONE.Method.PING)
  @Timed
  public String ping() {
    Health health = backend.health();
    if (health.isHealthy()) {
      return DTF.print(new DateTime());
    } else {
      LOG.error(health.getCause().getMessage(), health.getCause());
      throw new ServiceFailure("Unable to connect to back-end system");
    }
  }
}
