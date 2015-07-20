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
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;

/**
 * Operations relating to querying if a principle is authorized to perform an action.
 */
@Path("/mn/v1/isAuthorized")
@Singleton
public class IsAuthorizedResource {
  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public IsAuthorizedResource(AuthorizationManager auth) {
    this.auth = auth;
  }

  @GET
  @Path("isAuthorized/{pid}")
  @DataONE(DataONE.Method.IS_AUTHORIZED)
  @Timed
  public boolean isAuthorized(@Authenticate Session session, @PathParam("pid") String encodedId,
                              @QueryParam("action") Permission action) {
    String id = URLDecoder.decode(encodedId);
    auth.checkIsAuthorized(request, id, Permission.CHANGE_PERMISSION);
    return true;
  }

}
