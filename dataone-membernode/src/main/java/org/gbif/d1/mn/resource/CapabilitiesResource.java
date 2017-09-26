package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.CertificateUtils;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.exception.DataONE.Method;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Session;

/**
 * Operations relating to the capabilities of the node.
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
@Path("/mn/v1")
@Produces(MediaType.APPLICATION_XML)
@Singleton
public final class CapabilitiesResource {


  private final Node self;
  private final CertificateUtils certificateUtils;

  @Inject
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
   * A harmless utility to assist clients who might struggle with certificates.
   * This is NOT part of the DataONE API, but useful for operations folk.
   */
  @GET
  @Path("whoAmI")
  public Session debugCertificate(@Context HttpServletRequest request) {
    return certificateUtils.newSession(request, true);
  }
}
