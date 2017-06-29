package org.gbif.d1.mn.util;

import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Utliity class to write events to the MemberNode log.
 */
public class EventLogging {

  /**
   * Private constructor.
   */
  private EventLogging() {
    //NOP
  }
  //Type field used later by Logstash to route entries into different outputs
  private static final String LOG_TYPE = "dataonemn";

  /**
   * Writes an event into the events log.
   */
  public static void log(Logger logger, Session session, Identifier identifier, Event event, String message) {
    log(logger, session, identifier, event.value(), message);
  }

  /**
   * Writes an event into the events log.
   */
  public static void log(Logger logger, Session session, Identifier identifier, String event, String message) {
    MDC.put("subject", session.getSubject().getValue());
    MDC.put("event", event);
    MDC.put("identifier", identifier.getValue());
    MDC.put("type", LOG_TYPE);
    logger.info(message);
    MDC.clear();
  }

  /**
   * Writes an error event into log.
   */
  public static void logError(Logger logger, ExceptionDetail detail, Session session, String message) {
    MDC.put("event", Event.SYNCHRONIZATION_FAILED.value());
    MDC.put("identifier", detail.getPid());
    MDC.put("nodeId", detail.getNodeId());
    MDC.put("errorCode", Integer.toString(detail.getErrorCode()));
    MDC.put("detailCode", detail.getDetailCode());
    MDC.put("name", detail.getName());
    MDC.put("description", detail.getDescription());
    MDC.put("subject", session.getSubject().getValue());
    logger.error("Error synchronizing resources");
    MDC.clear();
  }
}
