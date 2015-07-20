package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.CertificateUtils;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.exception.DataONE.Method;
import org.gbif.d1.mn.rest.provider.Authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations relating to the request to replicate an object which is handled asynchronously.
 */
@Path("/mn/v1/replicate")
@Singleton
public class ReplicateResource {

  private static final Logger LOG = LoggerFactory.getLogger(ReplicateResource.class);
  private final EventBus queue;
  private final CertificateUtils certificateUtils;
  @Context
  private HttpServletRequest request;

  public ReplicateResource(EventBus queue, CertificateUtils certificateUtils) {
    this.queue = queue;
    this.certificateUtils = certificateUtils;
    queue.register(this);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.REPLICATE)
  @Timed
  public boolean replicate(
    @FormDataParam("sysmeta") SystemMetadata sysmeta,
    @FormDataParam("sourceNode") String sourceNode
  ) {
    checkNotNull(sysmeta, "Form parameter[sysmeta] is required");
    checkNotNull(sourceNode, "Form parameter[sourceNode] is required");

    // TODO: authorization
    Session session = certificateUtils.newSession(request);
    queue.post(new ReplicateEvent(sysmeta.getIdentifier().getValue(),
                                  sourceNode,
                                  request.getRemoteAddr(),
                                  request.getHeader("User-Agent"),
                                  session.getSubject()));
    return true;
  }

  @Subscribe
  final void replicate(ReplicateEvent event) {
    LOG.info("Received notification to replicate pid[{}] from sourceNode[{}]",
             event.getIdentifier(),
             event.getSourceNode());
    // TODO: the actual replication Ja
  }

  private static class ReplicateEvent {

    private final String identifier;
    private final String sourceNode;
    private final String ip;
    private final String userAgent;
    private final Subject subject;

    ReplicateEvent(String identifier, String sourceNode, String ip, String userAgent, Subject subject) {
      this.identifier = identifier;
      this.sourceNode = sourceNode;
      this.ip = ip;
      this.userAgent = userAgent;
      this.subject = subject;
    }

    public String getIdentifier() {
      return identifier;
    }

    public String getIp() {
      return ip;
    }

    public String getSourceNode() {
      return sourceNode;
    }

    public Subject getSubject() {
      return subject;
    }

    public String getUserAgent() {
      return userAgent;
    }
  }

  @POST
  @Path("replicate")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.REPLICATE)
  @Timed
  @Override
  public boolean replicate(@Authenticate Session session, @FormDataParam("sysmeta") SystemMetadata sysmeta,
                           @FormDataParam("sourceNode") String sourceNode) {
    checkIsSupported(replication);
    checkNotNull(sysmeta, "Form parameter[sysmeta] is required");
    checkNotNull(sourceNode, "Form parameter[sourceNode] is required");
    // eventBus.post(new ReplicateEvent(identifier, sourceNode, ip, userAgent, subject));
    return replication.replicate(session, sysmeta, sourceNode);
  }
}
