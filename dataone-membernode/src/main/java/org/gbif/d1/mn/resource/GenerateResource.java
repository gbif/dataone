package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.exception.DataONE;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.glassfish.jersey.media.multipart.FormDataParam;

import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations relating to the generation of identifiers.
 * <p>
 * All methods can throw:
 * <ul>
 * <li>{@link NotAuthorized} if the credentials presented do not have permission to perform the action</li>
 * <li>{@link InvalidToken} if the credentials in the request are not correctly presented</li>
 * <li>{@link ServiceFailure} if the system is unable to service the request</li>
 * <li>{@link NotImplemented} if the operation is unsupported</li>
 * </ul>
 *
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">The DataONE Member Node
 *      specification</a>
 */
@Path("/mn/v1/generate")
@Singleton
public final class GenerateResource {
  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public GenerateResource(AuthorizationManager auth) {this.auth = auth;}

  /**
   * Given a scheme and optional fragment, generates an identifier with that scheme and fragment that is unique.
   *
   * @throws InvalidRequest if any argument is null or fails validation
   */
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
