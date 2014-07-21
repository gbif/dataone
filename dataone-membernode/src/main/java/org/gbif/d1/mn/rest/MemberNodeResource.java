package org.gbif.d1.mn.rest;

import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.exception.DataONE.Method;
import org.gbif.d1.mn.rest.provider.Authenticate;
import org.gbif.d1.mn.util.D1Preconditions;

import java.io.InputStream;
import java.util.Date;

import javax.annotation.Nullable;
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
import com.google.common.collect.Lists;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.apis.v1.MNAuthorization;
import org.dataone.ns.service.apis.v1.MNRead;
import org.dataone.ns.service.apis.v1.MNReplication;
import org.dataone.ns.service.apis.v1.MNStorage;
import org.dataone.ns.service.apis.v1.MemberNode;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
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

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * A Tier 4 RESTful Member Node implementation.
 * <p>
 * This is a decorator that handles URL mapping and exception handling only delegating all implementation.
 * <p>
 * This is a singleton shared across the incoming request pool and therefore it is a requirement that this be
 * constructed with thread-safe references, which will make this class unconditionally thread-safe.
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

  private final MNRead read;
  private final MNAuthorization authorization;
  private final MNStorage storage;
  private final MNReplication replication;

  /**
   * Constructs a member node which will delegate to the provided services.
   * <p>
   * <strong>It is a requirement that all parameters are thread-safe</strong>
   * <p>
   * Should any service be null, then any subsequent API call that requires the service will return a
   * {@link NotImplemented} in accordance with the specification. This functionality is enforced by calling
   * {@link D1Preconditions#checkIsSupported(Object)}.
   * 
   * @param read to delegate Tier 1 requests to
   * @param authorization to delegate Tier 2 requests to
   * @param storage to delegate Tier 3 requests to
   * @param replication to delegate Tier 4 requests to
   * @throws NullPointerException if the read service is null - this is the minimum conformance level
   * @throws IllegalStateException if a service is provided that depends on a lower level service which is missing
   */
  public MemberNodeResource(MNRead read, MNAuthorization authorization, MNStorage storage,
    MNReplication replication) {
    // Note: Guava Preconditions here, not D1Preconditions
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
  public Identifier archive(@Authenticate Session session, @PathParam("pid") Identifier pid) {
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
    checkNotNull(pid, "Form parameter[pid] is required");
    checkNotNull(pid, "Form parameter[object] is required");
    checkNotNull(pid, "Form parameter[sysmeta] is required");
    return storage.create(session, pid, object, sysmeta);
  }

  @DELETE
  @Path("object/{pid}")
  @DataONE(Method.DELETE)
  @Timed
  @Override
  public Identifier delete(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(storage);
    return storage.delete(session, pid);
  }

  @HEAD
  @Path("object/{pid}")
  @DataONE(Method.DESCRIBE)
  @Timed
  @Override
  public DescribeResponse describe(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(read);
    return read.describe(session, pid);
  }

  @POST
  @Path("generate")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.GENERATE_IDENTIFIER)
  @Timed
  @Override
  public Identifier generateIdentifier(@Authenticate Session session, @FormDataParam("scheme") String scheme,
    @FormDataParam("fragment") String fragment) {
    checkIsSupported(storage);
    checkNotNull(scheme, "Form parameter[scheme] is required");
    checkNotNull(fragment, "Form parameter[fragment] is required");
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
  public Node getCapabilities() {
    checkIsSupported(read);
    return read.getCapabilities();
  }

  // Note: specification dictates /node returns same as /
  @GET
  @Path("node")
  @DataONE(Method.GET_CAPABILITIES)
  public Node getCapabilitiesWithNodePath() {
    return getCapabilities();
  }

  @GET
  @Path("checksum/{pid}")
  @DataONE(Method.GET_CHECKSUM)
  @Timed
  @Override
  public Checksum getChecksum(@Authenticate Session session, @PathParam("pid") Identifier pid,
    @QueryParam("checksumAlgorithm") String checksumAlgorithm) {
    checkIsSupported(read);
    checkNotNull(checksumAlgorithm, "Query parameter[checksumAlgorithm] is required");
    return read.getChecksum(session, pid, checksumAlgorithm);
  }

  @GET
  @Path("log")
  @DataONE(Method.GET_LOG_RECORDS)
  @Timed
  @Override
  public Log getLogRecords(@Authenticate Session session, @QueryParam("fromDate") Date fromDate,
    @QueryParam("toDate") Date toDate, @QueryParam("event") Event event,
    @QueryParam("pidFilter") @Nullable Identifier pidFilter, @QueryParam("start") @Nullable Integer start,
    @QueryParam("count") @Nullable Integer count) {
    checkIsSupported(read);
    Lists.newArrayList();
    checkNotNull(fromDate, "Query parameter[fromDate] is required");
    checkNotNull(fromDate, "Query parameter[toDate] is required");
    checkNotNull(fromDate, "Query parameter[eventDate] is required");
    return read.getLogRecords(session, fromDate, toDate, event, pidFilter, start, count);
  }

  @GET
  @Path("replica/{pid}")
  @DataONE(Method.GET_REPLICA)
  @Timed
  @Override
  public InputStream getReplica(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(read);
    return read.getReplica(session, pid);
  }

  @Override
  @GET
  @Path("meta/{pid}")
  @DataONE(Method.GET_SYSTEM_METADATA)
  @Timed
  public SystemMetadata getSystemMetadata(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(read);
    return read.getSystemMetadata(session, pid);
  }

  @GET
  @Path("isAuthorized/{pid}")
  @DataONE(Method.IS_AUTHORIZED)
  @Timed
  @Override
  public boolean isAuthorized(@Authenticate Session session, @PathParam("pid") Identifier pid,
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
    @QueryParam("toDate") @Nullable Date toDate, @QueryParam("formatId") @Nullable String formatId,
    @QueryParam("replicaStatus") @Nullable Boolean replicaStatus, @QueryParam("start") @Nullable Integer start,
    @QueryParam("count") @Nullable Integer count) {
    checkIsSupported(read);
    checkNotNull(fromDate, "Query parameter[fromDate] is required");
    return read.listObjects(session, fromDate, toDate, formatId, replicaStatus, start, count);
  }

  @GET
  @Path("monitor/ping")
  @Produces(MediaType.TEXT_PLAIN)
  @DataONE(Method.PING)
  @Timed
  @Override
  public String ping() {
    checkIsSupported(read);
    return read.ping();
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
    return replication.replicate(session, sysmeta, sourceNode);
  }

  @POST
  @Path("error")
  @DataONE(Method.SYNCHRONIZATION_FAILED)
  @Timed
  @Override
  public boolean synchronizationFailed(@Authenticate Session session, ExceptionDetail detail)
    throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure {
    checkIsSupported(read);
    checkNotNull(detail, "The exception detail is required");
    return read.synchronizationFailed(session, detail);
  }

  @POST
  @Path("dirtySystemMetadata")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.SYSTEM_METADATA_CHANGED)
  @Timed
  @Override
  public boolean systemMetadataChanged(@Authenticate Session session, @FormDataParam("pid") Identifier pid,
    @FormDataParam("serialVersion") long serialVersion,
    @FormDataParam("dateSystemMetadataLastModified") Date dateSystemMetadataLastModified) {
    checkIsSupported(authorization);
    checkNotNull(pid, "Form parameter[pid] is required");
    checkNotNull(serialVersion, "Form parameter[serialVersion] is required");
    checkNotNull(dateSystemMetadataLastModified, "Form parameter[dateSystemMetadataLastModified] is required");
    return read.systemMetadataChanged(session, pid, serialVersion, dateSystemMetadataLastModified);
  }

// @GET
// @Path("test")
// public String test(@Context HttpServletRequest request) {
// try {
// X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
// System.out.println(certs.length);
// System.out.println(certs[0].getSubjectDN());
// return "P: " + certs[0].getSubjectDN();
// } catch (Exception e) {
// e.printStackTrace();
// return "nope";
// }
// }

  @PUT
  @Path("object/{pid}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(Method.UPDATE)
  @Timed
  @Override
  public Identifier update(@Authenticate Session session, @PathParam("pid") Identifier pid,
    @FormDataParam("file") InputStream object, @FormDataParam("newPid") Identifier newPid,
    @FormDataParam("sysmeta") SystemMetadata sysmeta) {
    checkIsSupported(storage);
    checkNotNull(pid, "Form parameter[file] is required");
    checkNotNull(pid, "Form parameter[newPid] is required");
    checkNotNull(pid, "Form parameter[sysmeta] is required");
    return storage.update(session, pid, object, newPid, sysmeta);
  }
}