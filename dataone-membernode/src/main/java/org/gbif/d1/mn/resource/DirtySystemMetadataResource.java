package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.provider.Authenticate;
import org.gbif.d1.mn.exception.DataONE;

import java.util.Date;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.eventbus.EventBus;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.glassfish.jersey.media.multipart.FormDataParam;

import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations to handling notification that system metadata has changed.
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
@Path("/mn/v1/dirtySystemMetadata")
@Singleton
public final class DirtySystemMetadataResource {

  private final EventBus eventBus;

  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public DirtySystemMetadataResource(EventBus eventBus, AuthorizationManager auth) {
    this.eventBus = eventBus;
    this.auth = auth;
  }

  /**
   * Notifies the Member Node that the authoritative copy of system metadata on the Coordinating Nodes has changed.
   * <p>
   * The implementation should schedule an update to its information about the affected object by retrieving an
   * authoritative copy from a Coordinating Node. This can be accepted and return immediately provided the
   * implementation intends to perform the operation under normal circumstances.
   * <p>
   *
   * @return true if the request was accepted
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotFound if the DataONE object is not present on this node
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws ServiceFailure if the system is unable to service the request
   * @throws NotImplemented if the operation is unsupported
   */
  @POST
  @Path("dirtySystemMetadata")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(DataONE.Method.SYSTEM_METADATA_CHANGED)
  @Timed
  public boolean systemMetadataChanged(@Authenticate Session session, @FormDataParam("pid") Identifier pid,
                                       @FormDataParam("serialVersion") long serialVersion,
                                       @FormDataParam("dateSystemMetadataLastModified") Date dateSystemMetadataLastModified) {
    checkNotNull(pid, "Form parameter[pid] is required");
    checkNotNull(serialVersion, "Form parameter[serialVersion] is required");
    checkNotNull(dateSystemMetadataLastModified, "Form parameter[dateSystemMetadataLastModified] is required");
    auth.checkIsAuthorized(request, pid.getValue(), Permission.CHANGE_PERMISSION);
    return true;
  }
}
