package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.exception.DataONE.Method;
import org.gbif.d1.mn.provider.Authenticate;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.logging.EventLogging.log;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations related to the archival (hiding) of objects in DataONE.
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
@Path("/mn/v1/archive")
@Produces(MediaType.APPLICATION_XML)
@Singleton
public final class ArchiveResource {

  private static final Logger LOG = LoggerFactory.getLogger(ArchiveResource.class);

  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;
  private final MNBackend backend;

  public ArchiveResource(AuthorizationManager auth, MNBackend backend) {
    this.auth = auth;
    this.backend = backend;
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
  public Identifier archive(@Authenticate(optional = false) Session session, @PathParam("id") String encodedId) {
    LOG.info("Session subject {}", session.getSubject().getValue());
    checkNotNull(encodedId, "encodedId is required");
    Identifier id = Identifier.builder().withValue(URLDecoder.decode(encodedId)).build();
    log(LOG, session, id, Event.DELETE, "Archiving");
    backend.archive(session, id);
    return id;
  }

}
