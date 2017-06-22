package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.provider.Authenticate;
import org.gbif.d1.mn.exception.DataONE;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.codahale.metrics.annotation.Timed;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.LogEntry;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;


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

  private static final int DEFAULT_PAGE_SIZE =  20;
  private static final int DEFAULT_START =  0;

  //ElasticSearch client
  private final Client esClient;

  private final String logIdx;

  public LogResource(Client esClient, String logIdx) {
    this.esClient = esClient;
    this.logIdx = logIdx;
  }

  private static XMLGregorianCalendar toXmlGregorianCalendar(String date) {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from((ZonedDateTime.parse(date))));
    } catch (DatatypeConfigurationException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static LogEntry toLogEntry(SearchHit searchHit) {
    LogEntry.Builder builder = LogEntry.builder();
    Optional.ofNullable(searchHit.field("id"))
      .ifPresent(field -> builder.withEntryId(field.getValue()));
    Optional.ofNullable(searchHit.field("date"))
      .ifPresent(field -> builder.withDateLogged(toXmlGregorianCalendar(field.getValue())));
    Optional.ofNullable(searchHit.field("identifier"))
      .ifPresent(field -> builder.withIdentifier(Identifier.builder().withValue(field.getValue()).build()));
    Optional.ofNullable(searchHit.field("subject"))
      .ifPresent(field -> builder.withSubject(Subject.builder().withValue(field.getValue()).build()));
    Optional.ofNullable(searchHit.field("event"))
      .ifPresent(field -> builder.withEvent(Event.fromValue(field.getValue())));
    Optional.ofNullable(searchHit.field("ip_address"))
      .ifPresent(field -> builder.withIpAddress(field.getValue()).build());
    Optional.ofNullable(searchHit.field("node_identifier"))
      .ifPresent(field -> builder.withNodeIdentifier(NodeReference.builder().withValue(field.getValue()).build()).build());
    Optional.ofNullable(searchHit.field("user_agent"))
      .ifPresent(field -> builder.withUserAgent(field.getValue()).build());
    return builder.build();
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
  @Path("log")
  @DataONE(DataONE.Method.GET_LOG_RECORDS)
  @Timed
  public Log getLogRecords(@Authenticate Session session, @QueryParam("fromDate") Date fromDate,
                           @QueryParam("toDate") Date toDate, @QueryParam("event") Event event,
                           @QueryParam("pidFilter") @Nullable Identifier pidFilter, @QueryParam("start") @Nullable Integer start,
                           @QueryParam("count") @Nullable Integer count) {

    SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(logIdx).addSort("date", SortOrder.DESC);
    Integer realStart = Optional.of(start).orElse(DEFAULT_START);
    searchRequestBuilder.setFrom(realStart);
    searchRequestBuilder.setSize(Optional.ofNullable(count).orElse(DEFAULT_PAGE_SIZE));
    BoolQueryBuilder query =  QueryBuilders.boolQuery();
    Optional.ofNullable(event)
      .ifPresent(eventVal -> query.must().add(QueryBuilders.termQuery("event", eventVal.value())));
    Optional.ofNullable(pidFilter)
      .ifPresent(pidFilterVal -> query.must().add(QueryBuilders.termQuery("identifier", pidFilterVal)));
    Optional.of(fromDate)
      .ifPresent(fromDateVal -> query.must().add(QueryBuilders.rangeQuery("date")
                                                   .gte(fromDateVal).includeLower(Boolean.TRUE)));
    Optional.of(toDate)
      .ifPresent(toDateVal -> query.must().add(QueryBuilders.rangeQuery("date")
                                                   .lte(toDateVal).includeUpper(Boolean.TRUE)));
    searchRequestBuilder.setQuery(query);
    SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
    return Log.builder().withTotal(Long.valueOf(searchResponse.getHits().getTotalHits()).intValue())
                 .withStart(realStart)
                 .withCount(searchResponse.getHits().getHits().length)
                 .withLogEntry(Arrays.stream(searchResponse.getHits().getHits())
                                 .map(LogResource::toLogEntry)
                                 .collect(Collectors.toList())).build();
  }

}
