package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.provider.Authenticate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/**
 * Operations relating to querying if a principle is authorized to perform an action.
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
@Path("/mn/v1/isAuthorized")
@Singleton
public final class IsAuthorizedResource {
  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  private final MNBackend backend;

  @Inject
  public IsAuthorizedResource(AuthorizationManager auth, MNBackend backend) {
    this.auth = auth;
    this.backend = backend;
  }

  /**
   * Test if the client identified by the session is allowed to perform an operation at the stated permission level on
   * the specific object.
   *
   * @return true if the user is authorized, otherwise throws an exception
   * @throws NotFound if the DataONE object is not present on this node
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws ServiceFailure if the system is unable to service the request
   * @throws NotImplemented if the operation is unsupported
   */
  @GET
  @Path("{pid}")
  @DataONE(DataONE.Method.IS_AUTHORIZED)
  @Timed
  public boolean isAuthorized(@Authenticate Session session, @PathParam("pid") String encodedId,
                              @QueryParam("action") Permission action) {
    return auth.checkIsAuthorized(session, URLDecoder.decode(encodedId), action) != null;
  }

}
