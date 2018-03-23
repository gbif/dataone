package org.gbif.d1.mn.logging.impl;

import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.d1.mn.logging.LogSearchService;
import org.gbif.datarepo.persistence.mappers.LoggingMapper;
import org.gbif.datarepo.persistence.model.DBLoggingEvent;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.collect.ImmutableMap;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.LogEntry;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.logging.impl.Defaults.DEFAULT_START;
import static org.gbif.d1.mn.logging.impl.Defaults.DEFAULT_PAGE_SIZE;
import static org.gbif.d1.mn.logging.impl.Defaults.MAX_PAGE_SIZE;

/**
 * Log search services that queries the information stored by a logback DBAppender.
 */
public class LogbackDBLogSearchService implements LogSearchService {

  private static final Logger LOG = LoggerFactory.getLogger(LogbackDBLogSearchService.class);

  private final LoggingMapper loggingMapper;

  public LogbackDBLogSearchService(LoggingMapper loggingMapper) {
    this.loggingMapper = loggingMapper;
  }
  @Override
  public Log getLogRecords(DateTime fromDate, DateTime toDate, Event event, @Nullable String pidFilter,
                           Integer start, @Nullable Integer count) {
    PagingRequest pagingRequest = toPagingRequest(start, count);

    Map<String,String> mdcParam = toMDCMap(event, pidFilter);

    Long resultCount = loggingMapper.count(fromDate.getMillis(), toDate.getMillis(), mdcParam, pagingRequest);
    List<DBLoggingEvent> logs = loggingMapper.list(fromDate.getMillis(), toDate.getMillis(), mdcParam, pagingRequest);

    return Log.builder()
            .withCount(Optional.ofNullable(logs).map(List::size).orElse(0))
            .withStart((int)pagingRequest.getOffset())
            .withTotal(resultCount.intValue())
            .withLogEntry(toLogEntries(logs))
            .build();
  }

  /**
   * Converts even and pidFiler into a MDC map.
   *
   */
  private static Map<String,String> toMDCMap(Event event, @Nullable String pidFilter) {
    ImmutableMap.Builder<String,String> mdcBuilder = new ImmutableMap.Builder<>();
    Optional.ofNullable(event).ifPresent(eventVal -> mdcBuilder.put("event", eventVal.value()));
    Optional.ofNullable(pidFilter).ifPresent(pidFilterVal -> mdcBuilder.put("identifier", pidFilterVal));
    return mdcBuilder.build();
  }

  /**
   * Creates a {@link PagingRequest} using the start and count values.
   */
  private static PagingRequest toPagingRequest(Integer start, @Nullable Integer count) {
    Integer offset = Optional.ofNullable(start).orElse(DEFAULT_START);
    Integer limit = Math.min(Optional.ofNullable(count).orElse(DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
    return new PagingRequest(offset, limit);
  }

  /**
   * Converts a list of {@link DBLoggingEvent} into an Iterable of {@link LogEntry}.
   */
  private static Iterable<LogEntry> toLogEntries(List<DBLoggingEvent> dbLogs) {
    return dbLogs.stream().map(dbLoggingEvent -> {
                            LogEntry.Builder logEntryBuilder = LogEntry.builder()
                              .withEntryId(dbLoggingEvent.getEventId().toString());
                            Optional
                              .ofNullable(dbLoggingEvent.getMdc("event"))
                              .ifPresent(mdcEvent -> logEntryBuilder.withEvent(Event.fromValue(mdcEvent.getValue())));
                            Optional
                              .ofNullable(dbLoggingEvent.getMdc("identifier"))
                              .ifPresent(mdcIdentifier -> logEntryBuilder
                                .withIdentifier(Identifier.builder().withValue(mdcIdentifier.getValue()).build()));
                            Optional.ofNullable(dbLoggingEvent.getTimestamp())
                              .ifPresent(millis -> logEntryBuilder.withDateLogged(toXmlGregorianCalendar(millis)));
                            return logEntryBuilder.build();
                          }
          ).collect(Collectors.toList());
  }

  /**
   * Converts long values of epoch in milli seconds into a {@link XMLGregorianCalendar}.
   */
  private static XMLGregorianCalendar toXmlGregorianCalendar(long epoch) {
    try {
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(epoch);
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    } catch (DatatypeConfigurationException ex) {
      LOG.error("Error converting date {} into a XMLGregorianCalendar", epoch, ex);
      return null;
    }
  }
}
