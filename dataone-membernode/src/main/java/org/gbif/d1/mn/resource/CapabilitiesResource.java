package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.CertificateUtils;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.exception.DataONE.Method;

import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Session;

/**
 * Operations relating to the capabilities of the node.
 */
@Path("/mn/v1")
@Produces(MediaType.APPLICATION_XML)
@Singleton
public class CapabilitiesResource {

  private final Node self;
  private final CertificateUtils certificateUtils;

  public CapabilitiesResource(Node self, CertificateUtils certificateUtils) {
    this.self = self;
    this.certificateUtils = certificateUtils;
  }

  /**
   * Returns a document describing the capabilities of the Member Node.
   */
  @GET
  @DataONE(Method.GET_CAPABILITIES)
  @Timed
  public Node getCapabilities() {
    return self;
  }

  /**
   * The MN specification mandates this duplicate URL path.
   */
  @Path("node")
  @GET
  @DataONE(Method.GET_CAPABILITIES)
  public Node getCapabilitiesWithNodePath() {
    return getCapabilities();
  }

  /**
   * A harmful utility to assist clients who might struggle with certificates.
   * This is NOT part of the DataONE API, but useful for operations folk.
   */
  @GET
  @Path("whoAmI")
  public Session debugCertificate(@Context HttpServletRequest request) {
    return certificateUtils.newSession(request);
  }
}
