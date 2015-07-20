package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.eventbus.EventBus;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations to handling notification that system metadata has changed.
 */
@Path("/mn/v1/dirtySystemMetadata")
@Singleton
public class DirtySystemMetadataResource {

  private final EventBus eventBus;

  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public DirtySystemMetadataResource(EventBus eventBus, AuthorizationManager auth) {
    this.eventBus = eventBus;
    this.auth = auth;
  }

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
    // TODO: fire an internal event
    return true;
  }
}
