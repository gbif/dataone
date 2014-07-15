package org.gbif.d1.mn.rest;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.exception.DataONE.Method;
import org.gbif.d1.mn.rest.exception.PID;
import org.gbif.d1.mn.rest.provider.Authenticate;

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
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.apis.v1.MemberNode;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.SynchronizationFailed;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Tier 4 RESTful Member Node implementation.
 * <p>
 * This is a singleton shared across the incoming request pool and therefore it is a requirement that this be
 * constructed with thread-safe {@link AuthorizationManager} and {@link MNBackend}, which will make this class
 * unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance, as this implements the full DataONE API.
 * <p>
 * All top entry methods are annotated with {@link Timed} to enable performance monitoring and alerting using the
 * technology of your choice (e.g. JMX or pushing to Ganglia).
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
  private final Node self;

  public MemberNodeResource(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    this.backend = backend;
    this.self = self;
    this.authorizationManager = authorizationManager;
  }

  @PUT
  @Path("archive/{pid}")
  @Timed
  @Override
  public Identifier archive(@Authenticate Session session, @PathParam("pid") String pid) {
    return null;
  }

  @POST
  @Path("object")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.CREATE)
  @Timed
  @Override
  public Identifier create(@Authenticate Session session, @FormDataParam("pid") @PID String pid,
    @FormDataParam("object") InputStream object, @FormDataParam("sysmeta") SystemMetadata sysmeta) {
    Preconditions.checkNotNull(session, "Session is required");
    return Identifier.builder().withValue(pid).build();
  }

  @DELETE
  @Path("object/{pid}")
  @Timed
  @Override
  public Identifier delete(@Authenticate Session session, @PathParam("pid") String pid) {
    return null;
  }

  @HEAD
  @Path("object/{pid}")
  @Timed
  @Override
  public DescribeResponse describe(@Authenticate Session session, @PathParam("pid") String pid) {
    return null;
  }

  @POST
  @Path("generate")
  @Timed
  @Override
  public Identifier generateIdentifier(@Authenticate Session session, String scheme, String fragment) {
    return null;
  }

  @GET
  @Path("object/{pid}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Timed
  @Override
  public InputStream get(@Authenticate Session session, @PathParam("pid") String pid) {
    return null;
  }

  // The root ("/") resource
  @GET
  @Timed
  @Override
  public Node getCapabilities(@Authenticate Session session) {
    return self;
  }

  // Note: specification dictates /node returns same as /
  @GET
  @Path("node")
  public Node getCapabilitiesWithNodePath(@Authenticate Session session) {
    return getCapabilities(session);
  }

  @GET
  @Path("checksum/{pid}")
  @Timed
  @Override
  public Checksum getChecksum(@Authenticate Session session, @PathParam("pid") String pid,
    @QueryParam("checksumAlgorithm") String checksumAlgorithm) {
    return null;
  }

  @GET
  @Path("log")
  @Timed
  @Override
  public Log getLogRecords(@Authenticate Session session, @QueryParam("fromDate") Date fromDate,
    @QueryParam("toDate") Date toDate, @QueryParam("event") Event event, @QueryParam("pidFilter") String pidFilter,
    @QueryParam("start") Integer start, @QueryParam("count") Integer count) {
    return null;
  }

  @GET
  @Path("replica/{pid}")
  @Timed
  @Override
  public InputStream getReplica(@Authenticate Session session, @PathParam("pid") String pid) {
    return null;
  }

  @Override
  @GET
  @Path("meta/{pid}")
  @Timed
  public SystemMetadata getSystemMetadata(@Authenticate Session session, @PathParam("pid") String pid) {
    return null;
  }

  @GET
  @Path("isAuthorized/{pid}")
  @Timed
  @Override
  public boolean isAuthorized(@Authenticate Session session, @PathParam("pid") String pid,
    @QueryParam("action") Permission action) {
    return false;
  }

  @GET
  @Path("object")
  @Timed
  @Override
  public ObjectList listObjects(@Authenticate Session session, @QueryParam("fromDate") Date fromDate,
    @QueryParam("toDate") Date toDate, @QueryParam("formatId") String formatId,
    @QueryParam("replicaStatus") Boolean replicaStatus, @QueryParam("start") Integer start,
    @QueryParam("count") Integer count) {
    return null;
  }

  @GET
  @Path("monitor/ping")
  @Produces(MediaType.TEXT_PLAIN)
  @Timed
  @Override
  public String ping(@Authenticate Session session) {
    return DTF.print(new DateTime());
  }

  @POST
  @Path("replicate")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  @Override
  public boolean replicate(@Authenticate Session session, @FormDataParam("sysmeta") SystemMetadata sysmeta,
    @FormDataParam("sourceNode") String sourceNode) {
    return false;
  }

  @POST
  @Path("error")
  @Timed
  @Override
  public boolean synchronizationFailed(@Authenticate Session session, SynchronizationFailed message)
    throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure {
    return false;
  }

  @POST
  @Path("dirtySystemMetadata")
  @Timed
  @Override
  public boolean systemMetadataChanged(@Authenticate Session session, Identifier pid, long serialVersion,
    Date dateSystemMetadataLastModified) {
    return false;
  }

  @PUT
  @Path("object/{pid}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  @Override
  public Identifier update(@Authenticate Session session, @PathParam("pid") String pid,
    @FormDataParam("file") InputStream object, @FormDataParam("newPid") String newPid,
    @FormDataParam("sysmeta") SystemMetadata sysmeta) {
    return null;
  }
}