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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations relating to the generation of checksums.
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
@Path("/mn/v1/checksum")
@Singleton
public final class ChecksumResource {
  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;
  private final MNBackend backend;

  @Inject
  public ChecksumResource(AuthorizationManager auth, MNBackend backend) {
    this.auth = auth;
    this.backend = backend;
  }

  /**
   * Returns {@link Checksum} for the specified object using an accepted hashing algorithm. The result is used to
   * determine if two instances referenced by a PID are identical, hence it is necessary that MNs can ensure that the
   * returned checksum is valid for the referenced object either by computing it on the fly or by using a cached value
   * that is certain to be correct.
   *
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws NotFound if the DataONE object is not present on this node
   */
  @GET
  @Path("{pid}")
  @DataONE(DataONE.Method.GET_CHECKSUM)
  @Timed
  @Produces(MediaType.APPLICATION_XML)
  public Checksum getChecksum(@Authenticate Session session, @PathParam("pid") Identifier pid,
                              @QueryParam("checksumAlgorithm") String checksumAlgorithm) {
    auth.checkIsAuthorized(request, pid.getValue(), Permission.READ);
    return backend.checksum(pid, checksumAlgorithm);
  }

}
