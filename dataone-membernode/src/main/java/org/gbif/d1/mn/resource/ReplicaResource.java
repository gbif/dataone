package org.gbif.d1.mn.resource;

import org.dataone.ns.service.types.v1.Permission;
import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.provider.Authenticate;

import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.apis.v1.cn.CoordinatingNode;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.logging.EventLogging.log;
import static org.gbif.d1.mn.util.D1Preconditions.checkIsAuthorized;

/**
 * Operations relating to retrieval of a replica object.
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
@Path("/mn/v1/replica")
@Singleton
public final class ReplicaResource {

  private final MNBackend backend;
  private final AuthorizationManager auth;
  private final CoordinatingNode cn;

  @Context
  private HttpServletRequest request;

  private static final Logger LOG = LoggerFactory.getLogger(ReplicaResource.class);

  @Inject
  public ReplicaResource(MNBackend backend, AuthorizationManager auth, CoordinatingNode cn) {
    this.backend = backend;
    this.auth = auth;
    this.cn = cn;
  }

  @GET
  @Path("{pid}")
  @DataONE(DataONE.Method.GET_REPLICA)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Timed
  public InputStream getReplica(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    auth.checkIsAuthorized(session, pid.getValue(), Permission.READ);
    InputStream replica = backend.get(pid);
    log(LOG, session, pid, Event.REPLICATE, "Replicating object");
    return replica;
  }

}
