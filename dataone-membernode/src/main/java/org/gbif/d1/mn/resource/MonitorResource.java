package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.rest.exception.DataONE;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.spi.resource.Singleton;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;

/**
 * Operations relating to basic status monitoring.
 */
@Path("/mn/v1/monitor")
@Singleton
public class MonitorResource {
  @GET
  @Path("ping")
  @Produces(MediaType.TEXT_PLAIN) // the specification does not dictate a format, so opt for readability
  @DataONE(DataONE.Method.PING)
  @Timed
  public String ping() {
    return "TODO";
  }



}
