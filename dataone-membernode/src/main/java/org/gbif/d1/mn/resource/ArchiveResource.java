package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.exception.DataONE.Method;
import org.gbif.d1.mn.rest.provider.Authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;

/**
 * Operations related to the archival (hiding) of objects in DataONE.
 */
@Path("/mn/v1/archive")
@Produces(MediaType.APPLICATION_XML)
@Singleton
public final class ArchiveResource {

  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;

  public ArchiveResource(AuthorizationManager auth) {
    this.auth = auth;
  }

  /**
   * Hides an object managed by DataONE from search operations, effectively preventing its discovery during normal
   * operations.
   * <p>
   * The operation does not delete the object bytes, but instead sets the Types.SystemMetadata.archived flag to True.
   * This ensures that the object can still be resolved (and hence remain valid for existing citations and cross
   * references), though will not appear in searches.
   * <p>
   * Objects that are archived can not be updated through the MNStorage.update() operation.
   * <p>
   * Archived objects can not be un-archived. This behavior may change in future versions of the DataONE API.
   * <p>
   * Member Nodes MUST check that the caller is authorized to perform this function. if the object does not exist on the
   * node servicing the request, then an Exceptions.NotFound exception is raised. The message body of the exception
   * SHOULD contain a hint as to the location of the CNRead.resolve() method.
   *
   * @throws NotFound if the DataONE object is not present on this node
   */
  @PUT
  @Path("{id}")
  @DataONE(Method.ARCHIVE)
  @Timed
  public Identifier archive(@PathParam("id") String encodedId) {
    String id = URLDecoder.decode(encodedId);
    auth.checkIsAuthorized(request, id, Permission.CHANGE_PERMISSION);
    // TODO: stuff
    return null;
  }

}
