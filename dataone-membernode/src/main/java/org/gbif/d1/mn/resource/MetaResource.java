package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;

/**
 * Operations relating to retrieving system metadata.
 */
@Path("/mn/v1/meta")
@Singleton
public class MetaResource {
  @Override
  @GET
  @Path("meta/{pid}")
  @DataONE(DataONE.Method.GET_SYSTEM_METADATA)
  @Timed
  public SystemMetadata getSystemMetadata(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(read);
    return read.getSystemMetadata(session, pid);
  }
}
