package org.gbif.d1.mn.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a {@link Logger}.
 * <p>
 * TODO: consider NDCs?
 * <p>
 * TODO: this is terrible - make it an object so it can be injected for test purposes. Maybe we just inject the Logger?
 */
public class DataManagementLog {

  private static final Logger DATA_MANAGEMENT_LOG = LoggerFactory.getLogger(DataManagementLog.class);

  public static void error(String message, Object arg) {
    DATA_MANAGEMENT_LOG.error(message, arg);
  }
}
