package org.gbif.d1.mn.logging;


import java.util.Date;
import javax.annotation.Nullable;

import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;

/**
 * This interface encapsulates the search method(s) to discover information in
 */
public interface LogSearchService {

  /**
   * Retrieves the Log entries that match against the query parameters.
   * @param fromDate min date
   * @param toDate max date
   * @param event event type
   * @param pidFilter persistent id filter
   * @param start offset to start before returning results
   * @param count number of results to return
   * @return
   */
  Log getLogRecords(Date fromDate, Date toDate, Event event, @Nullable Identifier pidFilter, Integer start,
                    @Nullable Integer count);
}
