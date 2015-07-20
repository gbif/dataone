package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations relating to the generation of checksums.
 */
@Path("/mn/v1/checksum")
@Singleton
public class ChecksumResource {
  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public ChecksumResource(AuthorizationManager auth) {
    this.auth = auth;
  }

  @GET
  @Path("checksum/{pid}")
  @DataONE(DataONE.Method.GET_CHECKSUM)
  @Timed
  public Checksum getChecksum(@Authenticate Session session, @PathParam("pid") Identifier pid,
                              @QueryParam("checksumAlgorithm") String checksumAlgorithm) {
    checkNotNull(checksumAlgorithm, "Query parameter[checksumAlgorithm] is required");
    auth.checkIsAuthorized(request, pid.getValue(), Permission.READ);
    return null; // TODO
  }

}
