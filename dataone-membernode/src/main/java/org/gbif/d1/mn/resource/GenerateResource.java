package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations relating to the generation of identifiers.
 */
@Path("/mn/v1/generate")
@Singleton
public class GenerateResource {
  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public GenerateResource(AuthorizationManager auth) {this.auth = auth;}

  @POST
  @Path("generate")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(DataONE.Method.GENERATE_IDENTIFIER)
  @Timed
  public Identifier generateIdentifier(@FormDataParam("scheme") String scheme,
                                       @FormDataParam("fragment") String fragment) {
    checkNotNull(scheme, "Form parameter[scheme] is required");
    checkNotNull(fragment, "Form parameter[fragment] is required");
    Session session = auth.checkIsAuthorized(request, Permission.WRITE);
    return null; // TODO
  }
}
