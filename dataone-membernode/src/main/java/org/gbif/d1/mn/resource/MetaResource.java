package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.provider.Authenticate;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Operations relating to retrieving system metadata.
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
@Path("/mn/v1/meta")
@Singleton
public final class MetaResource {

  private final AuthorizationManager auth;
  private final MNBackend backend;

  public MetaResource(AuthorizationManager auth, MNBackend backend) {
    this.auth = auth;
    this.backend = backend;
  }

  /**
   * Describes the object identified by id by returning the associated system metadata object.
   *
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws NotFound if the DataONE object is not present on this node
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws InsufficientResources if the system determines that resource are exhausted
   */
  @GET
  @Path("{pid}")
  @DataONE(DataONE.Method.GET_SYSTEM_METADATA)
  @Timed
  public SystemMetadata getSystemMetadata(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    auth.checkIsAuthorized(session, pid.getValue(), Permission.READ);
    return backend.getSystemMetadata(session, pid);
  }
}
