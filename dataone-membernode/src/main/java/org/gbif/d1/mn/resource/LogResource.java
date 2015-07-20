package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.rest.exception.DataONE;
import org.gbif.d1.mn.rest.provider.Authenticate;

import java.util.Date;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.resource.Singleton;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.Session;

import static org.gbif.d1.mn.util.D1Preconditions.checkIsSupported;
import static org.gbif.d1.mn.util.D1Preconditions.checkNotNull;

/**
 * Operations relating to querying of audit log entries.
 */
@Path("/mn/v1/log")
@Singleton
public class LogResource {
  @GET
  @Path("log")
  @DataONE(DataONE.Method.GET_LOG_RECORDS)
  @Timed
  @Override
  public Log getLogRecords(@Authenticate Session session, @QueryParam("fromDate") Date fromDate,
                           @QueryParam("toDate") Date toDate, @QueryParam("event") Event event,
                           @QueryParam("pidFilter") @Nullable Identifier pidFilter, @QueryParam("start") @Nullable Integer start,
                           @QueryParam("count") @Nullable Integer count) {
    checkNotNull(fromDate, "Query parameter[fromDate] is required");
    checkNotNull(fromDate, "Query parameter[toDate] is required");
    checkNotNull(fromDate, "Query parameter[eventDate] is required");
    return read.getLogRecords(session, fromDate, toDate, event, pidFilter, start, count);
  }
}
