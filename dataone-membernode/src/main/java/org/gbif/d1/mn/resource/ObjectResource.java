package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

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
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations relating to CRUD operations on an Object.
 */
@Path("/mn/v1/object")
@Singleton
public class ObjectResource {
  @POST
  @Path("")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(DataONE.Method.CREATE)
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
  @DataONE(DataONE.Method.DELETE)
  @Timed
  @Override
  public Identifier delete(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(storage);
    return storage.delete(session, pid);
  }

  @HEAD
  @Path("object/{pid}")
  @DataONE(DataONE.Method.DESCRIBE)
  @Timed
  @Override
  public DescribeResponse describe(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(read);
    return read.describe(session, pid);
  }

  @GET
  @Path("object/{pid}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @DataONE(DataONE.Method.GET)
  @Timed
  @Override
  public InputStream get(@Authenticate Session session, @PathParam("pid") String pid) {
    checkIsSupported(read);
    return read.get(session, pid);
  }

  @GET
  @Path("object")
  @DataONE(DataONE.Method.LIST_OBJECTS)
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

  @PUT
  @Path("object/{pid}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @DataONE(DataONE.Method.UPDATE)
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
