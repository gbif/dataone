package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.codahale.metrics.annotation.Timed;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;

/**
 * Operations relating to retrieval of a replica object.
 */
@Path("/mn/v1/replica")
@Singleton
public class ReplicaResource {
  @GET
  @Path("replica/{pid}")
  @DataONE(DataONE.Method.GET_REPLICA)
  @Timed
  @Override
  public InputStream getReplica(@Authenticate Session session, @PathParam("pid") Identifier pid) {
    checkIsSupported(read);
    return read.getReplica(session, pid);
  }

}
