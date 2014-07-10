package org.gbif.d1.mn.rest;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import java.io.InputStream;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.apis.v1.MemberNode;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.SynchronizationFailed;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Tier 4 RESTful Member Node implementation.
 * <p>
 * This class is conditionally thread-safe. Should the {@link AuthorizationManager} and {@link MNBackend} be
 * thread-safe, as they should be, then this class is unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance, as this implements the full DataONE API.
 * <p>
 * All top entry methods are timed to enable performance monitoring and alerting.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">DataONE Member Node API</a>
 */
@Path("/mn/v1")
@Produces(MediaType.APPLICATION_XML)
@Singleton
public final class MemberNodeResource implements MemberNode {

  private static final Logger LOG = LoggerFactory.getLogger(MemberNodeResource.class);
  private final static String DATE_FORMAT = "HH:mm:ss Z 'on' EEE, MMM d, yyyy";
  @VisibleForTesting
  final static DateTimeFormatter DTF = DateTimeFormat.forPattern(DATE_FORMAT); // threadsafe

  private final AuthorizationManager authorizationManager;
  private final MNBackend backend;
  private final Node selfNode;

  public MemberNodeResource(MNBackend backend, Node selfNode) {
    this.backend = backend;
    this.selfNode = selfNode;
    this.authorizationManager = null; // TODO
  }

  @PUT
  @Path("archive/{pid}")
  @Timed
  @Override
  public Identifier archive(@Context Session session, @PathParam("pid") String pid) throws InvalidToken,
    ServiceFailure,
    NotAuthorized, NotFound, NotImplemented {
    return null;
  }

  @Override
  @POST
  @Path("object")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  public Identifier create(@Context Session session, @FormDataParam("pid") String pid,
    @FormDataParam("object") InputStream object,
    @FormDataParam("sysmeta") SystemMetadata sysmeta) throws IdentifierNotUnique, InsufficientResources,
    InvalidRequest, InvalidSystemMetadata, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure, UnsupportedType {

    return Identifier.builder().withValue(pid).build();
  }

  @DELETE
  @Path("object/{pid}")
  @Timed
  @Override
  public Identifier delete(@Context Session session, @PathParam("pid") String pid) throws InvalidToken, ServiceFailure,
    NotAuthorized, NotFound, NotImplemented {
    return null;
  }

  @HEAD
  @Path("object/{pid}")
  @Timed
  @Override
  public DescribeResponse describe(@Context Session session, @PathParam("pid") String pid) throws InvalidToken,
    NotAuthorized,
    NotImplemented, ServiceFailure, NotFound {
    return null;
  }

  @POST
  @Path("generate")
  @Timed
  @Override
  public Identifier generateIdentifier(@Context Session session, String scheme, String fragment) throws InvalidToken,
    ServiceFailure,
    NotAuthorized, NotImplemented, InvalidRequest {
    return null;
  }

  @GET
  @Path("object/{pid}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Timed
  @Override
  public InputStream get(@Context Session session, @PathParam("pid") String pid) throws InvalidToken, NotAuthorized,
    NotImplemented, ServiceFailure, NotFound, InsufficientResources {
    try {
      return null; // backend.get(securityContext.getUserPrincipal(), pid);
    } catch (Exception e) {
      throw logAndThrow(e, new ServiceFailure("1030", "Error retrieving data from the back end"));
    }
  }

  // The root ("/") resource
  @GET
  @Timed
  @Override
  public Node getCapabilities(@Context Session session) throws NotImplemented, ServiceFailure {
    return selfNode;
  }

  // Note: specification dictates /node returns same as /
  @GET
  @Path("node")
  public Node getCapabilitiesWithNodePath(@Context Session session) throws NotImplemented, ServiceFailure {
    return getCapabilities(session);
  }

  @GET
  @Path("checksum/{pid}")
  @Timed
  @Override
  public Checksum getChecksum(@Context Session session, @PathParam("pid") String pid,
    @QueryParam("checksumAlgorithm") String checksumAlgorithm) throws InvalidRequest, InvalidToken, NotAuthorized,
    NotImplemented, ServiceFailure, NotFound {
    return null;
  }

  @GET
  @Path("log")
  @Timed
  @Override
  public Log getLogRecords(@Context Session session, @QueryParam("fromDate") Date fromDate,
    @QueryParam("toDate") Date toDate,
    @QueryParam("event") Event event, @QueryParam("pidFilter") String pidFilter, @QueryParam("start") Integer start,
    @QueryParam("count") Integer count) throws InvalidRequest, InvalidToken, NotAuthorized, NotImplemented,
    ServiceFailure {
    // if (securityContext.getUserPrincipal() != null) {
    // LOG.error("Security context: {}", securityContext.getUserPrincipal().getName());
    // }

    // TODO: pass query in
    return null;
  }

  @GET
  @Path("replica/{pid}")
  @Timed
  @Override
  public InputStream getReplica(@Context Session session, @PathParam("pid") String pid) throws InvalidToken,
    NotAuthorized,
    NotImplemented, ServiceFailure, NotFound, InsufficientResources {
    return null;
  }

  @Override
  @GET
  @Path("meta/{pid}")
  @Timed
  public SystemMetadata getSystemMetadata(@Context Session session, @PathParam("pid") String pid) {
    return null;
  }

  @GET
  @Path("isAuthorized/{pid}")
  @Timed
  @Override
  public boolean isAuthorized(@Context Session session, @PathParam("pid") String pid,
    @QueryParam("action") Permission action)
    throws ServiceFailure, InvalidRequest, InvalidToken, NotFound, NotAuthorized, NotImplemented {
    // checkIsAuthorized(request, backend, action, pid, IS_AUTHORIZED_NOT_AUTHORIZED);
    return false;
  }

  @GET
  @Path("object")
  @Timed
  @Override
  public ObjectList listObjects(@Context Session session, @QueryParam("fromDate") Date fromDate,
    @QueryParam("toDate") Date toDate,
    @QueryParam("formatId") String formatId, @QueryParam("replicaStatus") Boolean replicaStatus,
    @QueryParam("start") Integer start, @QueryParam("count") Integer count) throws InvalidRequest, InvalidToken,
    NotAuthorized, NotImplemented, ServiceFailure {
    return null;
  }

  private <T extends Exception> T logAndThrow(Exception original, T t) throws T {
    LOG.error(original.getMessage(), original);
    throw t;
  }

  @GET
  @Path("monitor/ping")
  @Produces(MediaType.TEXT_PLAIN)
  @Timed
  @Override
  public String ping(@Context Session session) {

    return DTF.print(new DateTime());
  }

  @POST
  @Path("replicate")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  @Override
  public boolean replicate(@Context Session session, @FormDataParam("sysmeta") SystemMetadata sysmeta,
    @FormDataParam("sourceNode") String sourceNode) throws NotImplemented, ServiceFailure, NotAuthorized,
    InvalidRequest, InvalidToken, InsufficientResources, UnsupportedType {
    return false;
  }

  @POST
  @Path("error")
  @Timed
  @Override
  public boolean synchronizationFailed(@Context Session session, SynchronizationFailed message) throws InvalidToken,
    NotAuthorized,
    NotImplemented, ServiceFailure {
    return false;
  }

  @POST
  @Path("dirtySystemMetadata")
  @Timed
  @Override
  public boolean systemMetadataChanged(@Context Session session, Identifier pid, long serialVersion,
    Date dateSystemMetadataLastModified)
    throws InvalidToken, ServiceFailure, NotAuthorized, NotImplemented, InvalidRequest {
    return false;
  }

  @PUT
  @Path("object/{pid}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  @Override
  public Identifier update(@Context Session session, @PathParam("pid") String pid,
    @FormDataParam("file") InputStream object,
    @FormDataParam("newPid") String newPid, @FormDataParam("sysmeta") SystemMetadata sysmeta)
    throws IdentifierNotUnique, InsufficientResources, InvalidRequest, InvalidSystemMetadata, InvalidToken,
    NotAuthorized, NotImplemented, ServiceFailure, UnsupportedType, NotFound {
    return null;
  }
}