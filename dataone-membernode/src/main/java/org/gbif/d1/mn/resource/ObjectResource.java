package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.exception.DataONE;
import org.gbif.d1.mn.provider.Authenticate;

import java.io.InputStream;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
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
import com.google.common.base.Objects;
import io.dropwizard.jersey.params.DateTimeParam;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.dataone.ns.service.types.v1.Permission;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;
import static org.gbif.d1.mn.util.D1Preconditions.checkState;
import static org.gbif.d1.mn.util.D1Throwables.propagateOrServiceFailure;

/**
 * Operations relating to CRUD operations on an Object.
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
@Path("/mn/v1/object")
@Singleton
public final class ObjectResource {

  private static final Logger LOG = LoggerFactory.getLogger(ObjectResource.class);

  @Context
  private HttpServletRequest request;

  private final AuthorizationManager auth;
  private final MNBackend backend;

  /**
   * Writes entries into the log.
   */
  private static void log(Session session, Identifier identifier, Event event, String message) {
    MDC.put("subject", session.getSubject().getValue());
    MDC.put("event", event.value());
    MDC.put("identifier", identifier.getValue());
    MDC.put("type","dataonemn");
    LOG.info(message);
  }

  public ObjectResource(AuthorizationManager auth, MNBackend backend) {
    this.auth = auth;
    this.backend = backend;
  }

  /**
   * Called by a client to adds a new object to the Member Node.
   *
   * @throws IdentifierNotUnique if the identifier already exists within DataONE
   * @throws InsufficientResources if the system determines that resource are exhausted
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InvalidSystemMetadata if the system metadata is not well formed
   * @throws UnsupportedType if the supplied object type is not supported
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(DataONE.Method.CREATE)
  @Timed
  public Identifier create(@Authenticate Session session, @FormDataParam("pid") String pid,
                           @FormDataParam("object") InputStream object,
                           @FormDataParam("sysmeta") SystemMetadata sysmeta) {
    checkNotNull(pid, "Form parameter[pid] is required");
    checkNotNull(pid, "Form parameter[object] is required");
    checkNotNull(pid, "Form parameter[sysmeta] is required");
    checkState(Objects.equal(pid, sysmeta.getIdentifier().getValue()),
               "System metadata must have the correct identifier");
    // TODO: How do we decide who can create?
    // auth.checkIsAuthorized(request, Permission.WRITE);  // will fail, as only CN can create in current implementation
    try (InputStream in = object) {
      //Identifier identifier = backend.create(session, Identifier.builder().withValue(pid).build(), in, sysmeta);
      Identifier identifier = Identifier.builder().withValue(pid).build();
      log(session, identifier, Event.CREATE, "Resource created");
      LOG.info("Resource {} created", identifier);
      return identifier;
    } catch (Throwable e) {
      LOG.error("Error creating resource {}, with metadata {}", pid, sysmeta);
      throw propagateOrServiceFailure(e, "Unexpected error from backend system");
    }
  }

  /**
   * Deletes an object managed by DataONE from the Member Node. Member Nodes MUST check that the caller (typically a
   * Coordinating Node) is authorized to perform this function.
   * <p>
   * The delete operation will be used primarily by Coordinating Nodes to help manage the number of replicas of an
   * object that are present in the entire system.
   * <p>
   * The operation removes the object from further interaction with DataONE services. The implementation may delete the
   * object bytes, and in general should do so since a delete operation may be in response to a problem with the object
   * (e.g. it contains malicious content, is inappropriate, or is the subject of a legal request).
   * <p>
   * if the object does not exist on the node servicing the request, then an Exceptions.NotFound exception is raised.
   * The message body of the exception SHOULD contain a hint as to the location of the CNRead.resolve() method.
   *
   * @throws NotFound if the DataONE object is not present on this node
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   */
  @DELETE
  @Path("{pid}")
  @DataONE(DataONE.Method.DELETE)
  @Timed
  public Identifier delete(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    auth.checkIsAuthorized(session, pid.getValue(), Permission.WRITE);
    backend.delete(session, pid);
    log(session, pid, Event.DELETE, "Deleting resource");
    return pid;
  }

  /**
   * This method provides a lighter weight mechanism than {@link MNRead#getSystemMetadata(Session, String)} ()}
   * for a client to determine basic properties of the referenced object. The response should indicate properties that
   * are typically returned in a HTTP HEAD request: the date late modified, the size of the object, the type of the
   * object (the SystemMetadata.formatId).
   *
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws NotFound if the DataONE object is not present on this node
   */
  @HEAD
  @Path("{pid}")
  @DataONE(DataONE.Method.DESCRIBE)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Timed
  public DescribeResponse describe(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    return backend.describe(pid);
  }

  /**
   * Retrieve an object identified by id from the node.
   *
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws NotFound if the DataONE object is not present on this node
   * @throws InsufficientResources if the system determines that resource are exhausted
   */
  @GET
  @Path("{pid}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @DataONE(DataONE.Method.GET)
  @Timed
  public InputStream get(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    log(session, pid, Event.READ, "Resource read");
    auth.checkIsAuthorized(request, pid.getValue(), Permission.READ);
    InputStream inputStream = backend.get(pid);
    //log(session, pid, Event.READ, "Resource read");
    return  inputStream;
  }

  /**
   * Retrieve the list of objects present on the MN that match the calling parameters.
   * <p>
   * This method is required to support the process of Member Node synchronization. At a minimum, this method MUST be
   * able to return a list of objects that match "fromDate < SystemMetadata.dateSysMetadataModified". but is expected to
   * also support date range (by also specifying toDate), and should also support slicing of the matching set of records
   * by indicating the starting index of the response (where 0 is the index of the first item) and the count of elements
   * to be returned.
   * <p>
   * Note that date time precision is limited to one millisecond. if no timezone information is provided, the UTC will
   * be assumed.
   * <p>
   * Access control for this method MUST be configured to allow calling by Coordinating Nodes and MAY be configured to
   * allow more general access.
   *
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   */
  @GET
  @DataONE(DataONE.Method.LIST_OBJECTS)
  @Timed
  @Produces(MediaType.APPLICATION_XML)
  public ObjectList listObjects(@Authenticate Session session, @QueryParam("fromDate") DateTimeParam fromDate,
                                @QueryParam("toDate") @Nullable DateTimeParam toDate, @QueryParam("formatId") @Nullable String formatId,
                                @QueryParam("replicaStatus") @Nullable Boolean replicaStatus, @QueryParam("start") @Nullable Integer start,
                                @QueryParam("count") @Nullable Integer count) {
    checkNotNull(fromDate, "Query parameter[fromDate] is required");
    return backend.listObjects(null, Optional.ofNullable(fromDate).map(date -> date.get().toLocalDate().toDate()).orElse(null),
                               Optional.ofNullable(toDate).map(date -> date.get().toLocalDate().toDate()).orElse(null),
                               formatId, replicaStatus, start, count);
  }

  /**
   * This method is called by clients to update objects on Member Nodes.
   * <p>
   * Updates an existing object by creating a new object identified by newPid on the Member Node which explicitly
   * obsoletes the object identified by pid through appropriate changes to the SystemMetadata of pid and newPid.
   * <p>
   * The Member Node sets Types.SystemMetadata.obsoletedBy on the object being obsoleted to the pid of the new object.
   * It then updates Types.SystemMetadata.dateSysMetadataModified on both the new and old objects. The modified system
   * metadata entries then become available in MNRead.listObjects(). This ensures that a Coordinating Node will pick up
   * the changes when filtering on Types.SystemMetadata.dateSysMetadataModified.
   * <p>
   * The update operation MUST fail with Exceptions.InvalidRequest on objects that have the
   * Types.SystemMetadata.archived property set to true.
   * <p>
   * A new, unique Types.SystemMetadata.seriesId may be included when beginning a series, or a series may be extended if
   * the newPid obsoletes the existing pid.
   *
   * @throws IdentifierNotUnique if the identifier already exists within DataONE
   * @throws InsufficientResources if the system determines that resource are exhausted
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InvalidSystemMetadata if the system metadata is not well formed
   * @throws UnsupportedType if the supplied object type is not supported
   * @throws NotFound if the DataONE object is not present on this node
   */
  @PUT
  @Path("{pid}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(DataONE.Method.UPDATE)
  @Timed
  public Identifier update(@Authenticate Session session, @PathParam("pid") Identifier pid,
                           @FormDataParam("file") InputStream object, @FormDataParam("newPid") Identifier newPid,
                           @FormDataParam("sysmeta") SystemMetadata sysmeta) {
    checkNotNull(pid, "Form parameter[file] is required");
    checkNotNull(pid, "Form parameter[newPid] is required");
    checkNotNull(pid, "Form parameter[sysmeta] is required");
    Identifier identifier = backend.update(session, pid, object, newPid, sysmeta);
    log(session, pid, Event.CREATE, "Resource updated");
    return  identifier;
  }
}
