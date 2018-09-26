package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.logging.LogSearchService;
import org.gbif.d1.mn.provider.Authenticate;
import org.gbif.d1.mn.exception.DataONE;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.DateTimeParam;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operations relating to querying of audit log entries.
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
@Path("/mn/v1/log")
@Singleton
public final class LogResource {

  private static final Logger LOG = LoggerFactory.getLogger(LogResource.class);

  private final LogSearchService logSearchService;

  private final AuthorizationManager auth;

  public LogResource(LogSearchService logSearchService, AuthorizationManager auth) {
    this.logSearchService = logSearchService;
    this.auth = auth;
  }


  /**
   * Retrieve log information from the Member Node for the specified slice parameters. Log entries will only return
   * PIDs.
   *
   * @throws InvalidRequest if any argument is null or fails validation
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   */
  @GET
  @DataONE(DataONE.Method.GET_LOG_RECORDS)
  @Timed
  public Log getLogRecords(@Authenticate Session session, @QueryParam("fromDate") DateTimeParam fromDate,
                           @QueryParam("toDate") DateTimeParam toDate, @QueryParam("event") Event event,
                           @QueryParam("idFilter") @Nullable String pidFilter, @QueryParam("start") @Nullable Integer start,
                           @QueryParam("count") @Nullable Integer count) {
    return logSearchService.getLogRecords(Optional.ofNullable(fromDate).map(DateTimeParam::get).orElse(null),
                                          Optional.ofNullable(toDate).map(DateTimeParam::get).orElse(null),
                                          event, pidFilter, start, count);
  }

}
