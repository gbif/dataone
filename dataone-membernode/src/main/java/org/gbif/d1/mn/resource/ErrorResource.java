package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations for handling notifications from a CN that an error has occurred.
 */
@Path("/mn/v1/error")
@Singleton
public class ErrorResource {
  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public ErrorResource(AuthorizationManager auth) {this.auth = auth;}

  /**
   * A notification from the CN of a failure and we simply write to the audit log.
   * The specification mandates we return a boolean and HTTP 200 on success although this is pointless as anything else
   * would surface as an Exception here and be returned as a non HTTP 200 code.
   */
  @POST
  @Path("error")
  @DataONE(DataONE.Method.SYNCHRONIZATION_FAILED)
  @Timed
  public boolean synchronizationFailed(@Authenticate Session session, ExceptionDetail detail)
    throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure {
    checkNotNull(detail, "The exception detail is required");
    auth.checkIsAuthorized(request, Permission.CHANGE_PERMISSION);
    // TODO: write to an audit log
    return true; // pointless, but the specification mandates it
  }
}
