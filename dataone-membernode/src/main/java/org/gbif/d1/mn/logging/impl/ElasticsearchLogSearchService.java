package org.gbif.d1.mn.logging.impl;

import org.gbif.d1.mn.logging.LogSearchService;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.LogEntry;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.Subject;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

/**
 * log search service backed by an Elasticsearch/logstash index.
 */
public class ElasticsearchLogSearchService implements LogSearchService {

  private static final int MAX_PAGE_SIZE =  1000;

  private static final int DEFAULT_PAGE_SIZE =  20;
  private static final int DEFAULT_START =  0;

  //ElasticSearch client
  private final Client esClient;

  private final String logIdx;

  /**
   * Full constructor.
   * @param esClient ElasticSearch client
   * @param logIdx elasticsearch index
   */
  public ElasticsearchLogSearchService(Client esClient, String logIdx) {
    this.esClient = esClient;
    this.logIdx = logIdx;
  }

  /**
   * Translates a date string into a XMLGregorianCalendar.
   */
  private static XMLGregorianCalendar toXmlGregorianCalendar(String date) {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.parse(date)));
    } catch (DatatypeConfigurationException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Translates an ElasticSearch SearchHit into a LogEntry.
   */
  private static LogEntry toLogEntry(SearchHit searchHit) {
    LogEntry.Builder<Void> builder = LogEntry.builder();
    builder.withEntryId(searchHit.id());
    Map<String,Object> source =  searchHit.getSource();
    Optional.ofNullable(source.get("@timestamp"))
      .ifPresent(field -> builder.withDateLogged(toXmlGregorianCalendar(field.toString())));
    Optional.ofNullable(source.get("identifier"))
      .ifPresent(field -> builder.withIdentifier(Identifier.builder().withValue(field.toString()).build()));
    Optional.ofNullable(source.get("subject"))
      .ifPresent(field -> builder.withSubject(Subject.builder().withValue(field.toString()).build()));
    Optional.ofNullable(source.get("event"))
      .ifPresent(field -> builder.withEvent(Event.fromValue(field.toString())));
    Optional.ofNullable(source.get("host"))
      .ifPresent(field -> builder.withIpAddress(field.toString()).build());
    Optional.ofNullable(source.get("node_identifier"))
      .ifPresent(field -> builder.withNodeIdentifier(NodeReference.builder().withValue(field.toString()).build()).build());
    Optional.ofNullable(source.get("user_agent"))
      .ifPresent(field -> builder.withUserAgent(field.toString()).build());
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
   @Override
   public Log getLogRecords(Date fromDate, Date toDate, Event event, @Nullable Identifier pidFilter,
                           @Nullable Integer start, @Nullable Integer count) {

    SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(logIdx).addSort("@timestamp", SortOrder.DESC);
    Integer realStart = Optional.ofNullable(start).orElse(DEFAULT_START);
    searchRequestBuilder.setFrom(realStart);
    searchRequestBuilder.setSize(Math.min(Optional.ofNullable(count).orElse(DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE));
    BoolQueryBuilder query =  QueryBuilders.boolQuery();
    Optional.ofNullable(event)
      .ifPresent(eventVal -> query.must().add(QueryBuilders.termQuery("event", eventVal.value())));
    Optional.ofNullable(pidFilter)
      .ifPresent(pidFilterVal ->
                   Optional.ofNullable(pidFilterVal.getValue())
                     .ifPresent(pidValue -> query.must().add(QueryBuilders.termQuery("identifier", pidValue))));
    Optional.ofNullable(fromDate)
      .ifPresent(fromDateVal -> query.must().add(QueryBuilders.rangeQuery("@timestamp")
                                                   .gte(fromDateVal).includeLower(Boolean.TRUE)));
    Optional.ofNullable(toDate)
      .ifPresent(toDateVal -> query.must().add(QueryBuilders.rangeQuery("@timestamp")
                                                 .lte(toDateVal).includeUpper(Boolean.TRUE)));
    searchRequestBuilder.setQuery(query);
    SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
    return Log.builder().withTotal(Long.valueOf(searchResponse.getHits().getTotalHits()).intValue())
            .withStart(realStart)
            .withCount(searchResponse.getHits().getHits().length)
            .withLogEntry(Arrays.stream(searchResponse.getHits().getHits())
                            .map(ElasticsearchLogSearchService::toLogEntry)
                            .collect(Collectors.toList())).build();
  }

}
