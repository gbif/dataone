package org.gbif.d1.mn.rest;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.exception.DataONE.Method;
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
import com.google.common.base.Preconditions;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.apis.v1.MemberNode;
import org.dataone.ns.service.apis.v1.MemberNodeAuthorization;
import org.dataone.ns.service.apis.v1.MemberNodeRead;
import org.dataone.ns.service.apis.v1.MemberNodeReplication;
import org.dataone.ns.service.apis.v1.MemberNodeStorage;
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

/**
 * A Tier 4 RESTful Member Node implementation.
 * <p>
 * This is a decorator that handles URL mapping and exception handling only delegating all implementation.
 * <p>
 * This is a singleton shared across the incoming request pool and therefore it is a requirement that this be
 * constructed with thread-safe {@link AuthorizationManager} and {@link MNBackend}, which will make this class
 * unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance, as this implements the full DataONE API.
 * <p>
 * All top entry methods are annotated with {@link Timed} to enable performance monitoring and alerting using the
 * technology of your choice (e.g. JMX or pushing to Ganglia).
 * <p>
 * Note: we inject {@link Session} on each method rather than once for the class for testability purposes. It allows us
 * to use the Jersey InMemory test container, whereas this is not possible if we move this to a field. Designing for
 * solid and fast testing was a priority in when developing the software architecture.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">DataONE Member Node API</a>
 */
@Path("/mn/v1")
@Produces(MediaType.APPLICATION_XML)
@Singleton
public final class MemberNodeResource implements MemberNode {

  private final MemberNodeRead read;
  private final MemberNodeAuthorization authorization;
  private final MemberNodeStorage storage;
  private final MemberNodeReplication replication;

  /**
   * Constructs a member node with the delegate backend services.
   * <p>
   * Should any service be null, then any subsequent API call that requires the service will return a
   * {@link NotImplemented} in accordance with the spec.
   * 
   * @throws NullPointerException if the read service is null - this is the minimum conformance level
   * @throws IllegalStateException if a service is provided that depends on a lower level service which is missing
   */
  public MemberNodeResource(MemberNodeRead read, MemberNodeAuthorization authorization, MemberNodeStorage storage,
    MemberNodeReplication replication) {
    Preconditions.checkNotNull(read, "Read service (Tier 1) is required at a minimum");
    Preconditions.checkState(storage == null || authorization != null,
      "Cannot implement storage (Tier 3) without authorization (Tier 2)");
    Preconditions.checkState(replication == null || storage != null,
      "Cannot implement replication (Tier 4) without storage (Tier 3)");
    this.read = read;
    this.authorization = authorization;
    this.storage = storage;
    this.replication = replication;
  }

  @PUT
  @Path("archive/{pid}")
  @DataONE(Method.ARCHIVE)
  @Timed
  @Override
  public Identifier archive(@Authenticate Session session, @PathParam("pid") String pid) {
    checkIsSupported(storage);
    return storage.archive(session, pid);
  }

  @POST
  @Path("object")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.CREATE)
  @Timed
  @Override
  public Identifier create(@Authenticate Session session, @FormDataParam("pid") String pid,
    @FormDataParam("object") InputStream object, @FormDataParam("sysmeta") SystemMetadata sysmeta) {
    checkIsSupported(storage);
    return storage.create(session, pid, object, sysmeta);
  }

  @DELETE
  @Path("object/{pid}")
  @DataONE(Method.DELETE)
  @Timed
  @Override
  public Identifier delete(@Authenticate Session session, @PathParam("pid") String pid) {
    checkIsSupported(storage);
    return storage.delete(session, pid);
  }

  @HEAD
  @Path("object/{pid}")
  @DataONE(Method.DESCRIBE)
  @Timed
  @Override
  public DescribeResponse describe(@Authenticate Session session, @PathParam("pid") String pid) {
    checkIsSupported(read);
    return read.describe(session, pid);
  }

  @POST
  @Path("generate")
  @DataONE(Method.GENERATE_IDENTIFIER)
  @Timed
  @Override
  public Identifier generateIdentifier(@Authenticate Session session, String scheme, String fragment) {
    checkIsSupported(storage);
    return storage.generateIdentifier(session, scheme, fragment);
  }

  @GET
  @Path("object/{pid}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @DataONE(Method.GET)
  @Timed
  @Override
  public InputStream get(@Authenticate Session session, @PathParam("pid") String pid) {
    checkIsSupported(read);
    return read.get(session, pid);
  }

  // The root ("/") resource
  @GET
  @DataONE(Method.GET_CAPABILITIES)
  @Timed
  @Override
  public Node getCapabilities(@Authenticate Session session) {
    checkIsSupported(read);
    return read.getCapabilities(session);
  }

  // Note: specification dictates /node returns same as /
  @GET
  @Path("node")
  @DataONE(Method.GET_CAPABILITIES)
  public Node getCapabilitiesWithNodePath(@Authenticate Session session) {
    return getCapabilities(session);
  }

  @GET
  @Path("checksum/{pid}")
  @DataONE(Method.GET_CHECKSUM)
  @Timed
  @Override
  public Checksum getChecksum(@Authenticate Session session, @PathParam("pid") String pid,
    @QueryParam("checksumAlgorithm") String checksumAlgorithm) {
    checkIsSupported(read);
    return read.getChecksum(session, pid, checksumAlgorithm);
  }

  @GET
  @Path("log")
  @DataONE(Method.GET_LOG_RECORDS)
  @Timed
  @Override
  public Log getLogRecords(@Authenticate Session session, @QueryParam("fromDate") Date fromDate,
    @QueryParam("toDate") Date toDate, @QueryParam("event") Event event, @QueryParam("pidFilter") String pidFilter,
    @QueryParam("start") Integer start, @QueryParam("count") Integer count) {
    checkIsSupported(read);
    return read.getLogRecords(session, fromDate, toDate, event, pidFilter, start, count);
  }

  @GET
  @Path("replica/{pid}")
  @DataONE(Method.GET_REPLICA)
  @Timed
  @Override
  public InputStream getReplica(@Authenticate Session session, @PathParam("pid") String pid) {
    checkIsSupported(read);
    return read.getReplica(session, pid);
  }

  @Override
  @GET
  @Path("meta/{pid}")
  @DataONE(Method.GET_SYSTEM_METADATA)
  @Timed
  public SystemMetadata getSystemMetadata(@Authenticate Session session, @PathParam("pid") String pid) {
    checkIsSupported(read);
    return read.getSystemMetadata(session, pid);
  }

  @GET
  @Path("isAuthorized/{pid}")
  @DataONE(Method.IS_AUTHORIZED)
  @Timed
  @Override
  public boolean isAuthorized(@Authenticate Session session, @PathParam("pid") String pid,
    @QueryParam("action") Permission action) {
    checkIsSupported(authorization);
    return authorization.isAuthorized(session, pid, action);
  }

  @GET
  @Path("object")
  @DataONE(Method.LIST_OBJECTS)
  @Timed
  @Override
  public ObjectList listObjects(@Authenticate Session session, @QueryParam("fromDate") Date fromDate,
    @QueryParam("toDate") Date toDate, @QueryParam("formatId") String formatId,
    @QueryParam("replicaStatus") Boolean replicaStatus, @QueryParam("start") Integer start,
    @QueryParam("count") Integer count) {
    checkIsSupported(read);
    return read.listObjects(session, fromDate, toDate, formatId, replicaStatus, start, count);
  }

  @GET
  @Path("monitor/ping")
  @Produces(MediaType.TEXT_PLAIN)
  @DataONE(Method.PING)
  @Timed
  @Override
  public String ping(@Authenticate Session session) {
    checkIsSupported(read);
    return read.ping(session);
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
    return replication.replicate(session, sysmeta, sourceNode);
  }

  @POST
  @Path("error")
  @DataONE(Method.SYNCHRONIZATION_FAILED)
  @Timed
  @Override
  public boolean synchronizationFailed(@Authenticate Session session, SynchronizationFailed message)
    throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure {
    checkIsSupported(read);
    return read.synchronizationFailed(session, message);
  }

  @POST
  @Path("dirtySystemMetadata")
  @DataONE(Method.SYSTEM_METADATA_CHANGED)
  @Timed
  @Override
  public boolean systemMetadataChanged(@Authenticate Session session, Identifier pid, long serialVersion,
    Date dateSystemMetadataLastModified) {
    checkIsSupported(authorization);
    return authorization.systemMetadataChanged(session, pid, serialVersion, dateSystemMetadataLastModified);
  }

  @PUT
  @Path("object/{pid}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.UPDATE)
  @Timed
  @Override
  public Identifier update(@Authenticate Session session, @PathParam("pid") String pid,
    @FormDataParam("file") InputStream object, @FormDataParam("newPid") String newPid,
    @FormDataParam("sysmeta") SystemMetadata sysmeta) {
    checkIsSupported(storage);
    return storage.update(session, pid, object, newPid, sysmeta);
  }

  /**
   * @throws NotImplemented If service is null
   */
  private void checkIsSupported(Object service) {
    if (service == null) {
      throw new NotImplemented("This node not configured to support operation");
    }
  }
}