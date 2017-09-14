package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.provider.Authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;
import static org.gbif.d1.mn.logging.EventLogging.logError;

/**
 * Operations for handling notifications from a CN that an error has occurred.
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
@Path("/mn/v1/error")
public final class ErrorResource {

  private static final Logger LOG = LoggerFactory.getLogger(ErrorResource.class);

  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public ErrorResource(AuthorizationManager auth) {this.auth = auth;}

  /**
   * This is a callback method used by a CN to indicate to a MN that it cannot complete synchronization of the science
   * metadata identified by pid. When called, the MN should take steps to record the problem description and notify an
   * administrator or the data owner of the issue.
   *
   * The specification mandates we return a boolean and HTTP 200 on success although this is pointless as anything else
   * would surface as an Exception here and be returned as a non HTTP 200 code.
   *
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   */
  @POST
  @DataONE(DataONE.Method.SYNCHRONIZATION_FAILED)
  @Timed
  public boolean synchronizationFailed(@Authenticate(optional = false) Session session, ExceptionDetail detail)
    throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure {
    checkNotNull(detail, "The exception detail is required");
    auth.checkIsAuthorized(request, Permission.CHANGE_PERMISSION);
    logError(LOG, detail, session, "Error synchronizing resources");
    return true; // pointless, but the specification mandates it
  }
}
