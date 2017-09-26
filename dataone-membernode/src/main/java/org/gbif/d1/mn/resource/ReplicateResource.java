package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.client.MNReadClient;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.exception.DataONE.Method;
import org.gbif.d1.mn.provider.Authenticate;

import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.dropwizard.client.JerseyClientBuilder;
import org.dataone.ns.service.apis.v1.cn.CoordinatingNode;
import org.dataone.ns.service.apis.v1.mn.MNRead;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;
import static org.gbif.d1.mn.util.D1Preconditions.checkIsAuthorized;

/**
 * Operations relating to the request to replicate an object which is handled asynchronously.
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
@Path("/mn/v1/replicate")
@Singleton
public final class ReplicateResource {

  private static final Logger LOG = LoggerFactory.getLogger(ReplicateResource.class);
  private final EventBus queue;
  private final JerseyClientBuilder clientBuilder;
  private final CoordinatingNode cnClient;
  private final AuthorizationManager authorizationManager;
  private final MNBackend mnBackend;

  @Context
  private HttpServletRequest request;

  /**
   * Writes entries into the log.
   */
  private static void log(Session session, Identifier identifier, Event event, String message) {
    MDC.put("subject", session.getSubject().getValue());
    MDC.put("event", event.value());
    MDC.put("identifier", identifier.getValue());
    LOG.info(message);
  }

  @Inject
  public ReplicateResource(EventBus queue, JerseyClientBuilder clientBuilder,
                           CoordinatingNode cnClient,
                           AuthorizationManager authorizationManager,
                           MNBackend mnBackend) {
    this.queue = queue;
    this.clientBuilder = clientBuilder;
    this.cnClient = cnClient;
    this.authorizationManager = authorizationManager;
    this.mnBackend = mnBackend;
    queue.register(this);
  }

  /**
   * Contacts the coordinating node to get node's capabilities.
   */
  private Node getSourceNode(String sourceNode) {
    return cnClient.getNodeCapabilities(sourceNode);
  }
  /**
   * Called by a Coordinating Node to request that the Member Node create a copy of the specified object by retrieving
   * it from another Member Node and storing it locally so that it can be made accessible to the DataONE system.
   * <p>
   * A successful operation is indicated by a HTTP status of 200 on the response.
   * <p>
   * Failure of the operation MUST be indicated by returning an appropriate exception.
   * <p>
   * Access control for this method MUST be configured to allow calling by Coordinating Nodes.
   *
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InsufficientResources if the system determines that resource are exhausted
   * @throws UnsupportedType if the supplied object type is not supported
   * @throws ServiceFailure if the system is unable to service the request
   * @throws NotImplemented if the operation is unsupported
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.REPLICATE)
  @Timed
  public boolean replicate(@Authenticate(optional = false) Session session,
                           @FormDataParam("sysmeta") SystemMetadata sysmeta,
                           @FormDataParam("sourceNode") String sourceNode) {
    checkNotNull(sysmeta, "Form parameter[sysmeta] is required");
    checkNotNull(sourceNode, "Form parameter[sourceNode] is required");
    checkIsAuthorized(authorizationManager.isAuthorityNodeOrCN(session.getSubject().getValue(), sysmeta),
                      "Replication has to be triggered by a trusted subject");
    Node sourceMnNode = getSourceNode(sourceNode);
    queue.post(new ReplicateEvent(sysmeta.getIdentifier(),
                                  sourceMnNode,
                                  sysmeta,
                                  request.getRemoteAddr(),
                                  request.getHeader("User-Agent"),
                                  session.getSubject()));
    log(session, sysmeta.getIdentifier(), Event.REPLICATE, "Replicating resource");
    return true;
  }

  @Subscribe
  final void replicate(ReplicateEvent event) {
    LOG.info("Replication event received: {}", event);
    try {
      MNRead mnRead = new MNReadClient(clientBuilder.build(UUID.randomUUID().toString()), event.getSourceNode().getBaseURL());
      mnBackend.create(Session.builder().withSubject(event.getSubject()).build(),
                       event.getIdentifier(),
                       mnRead.get(event.getIdentifier()),
                       event.getSystemMetadata());
    } catch (Exception ex) {
      LOG.error("Error processing replication event", ex);
    }

  }

  /**
   * Object used to encapsulate the message sent from a replication request to the actual processing of it.
   */
  private static class ReplicateEvent {

    private final Identifier identifier;
    private final Node sourceNode;
    private final SystemMetadata systemMetadata;
    private final String ip;
    private final String userAgent;
    private final Subject subject;

    ReplicateEvent(Identifier identifier, Node sourceNode, SystemMetadata systemMetadata,
                   String ip, String userAgent, Subject subject) {
      this.identifier = identifier;
      this.sourceNode = sourceNode;
      this.systemMetadata = systemMetadata;
      this.ip = ip;
      this.userAgent = userAgent;
      this.subject = subject;
    }

    public Identifier getIdentifier() {
      return identifier;
    }

    public String getIp() {
      return ip;
    }

    public Node getSourceNode() {
      return sourceNode;
    }

    public SystemMetadata getSystemMetadata() {
      return systemMetadata;
    }

    public Subject getSubject() {
      return subject;
    }

    public String getUserAgent() {
      return userAgent;
    }

    @Override
    public String toString() {
      return "ReplicateEvent{" +
             "identifier=" + identifier +
             ", sourceNode=" + sourceNode +
             ", systemMetadata=" + systemMetadata +
             ", ip='" + ip + '\'' +
             ", userAgent='" + userAgent + '\'' +
             ", subject=" + subject +
             '}';
    }
  }
}
