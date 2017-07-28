package org.gbif.d1.mn.backend.memory;

import java.util.Date;
import java.util.function.Predicate;

import com.google.common.base.Objects;
import org.dataone.ns.service.types.v1.NodeReference;

/**
 * Static utility methods pertaining to Predicate instances used for filtering the in-memory data.
 */
class Filters {

  private static Predicate<PersistedObject> alwaysTrue() {
    return p -> true;
  }

  static Predicate<PersistedObject> after(final Date earliest) {
    return earliest == null ? alwaysTrue() : input -> {
        try {
          return input.getSysmeta().getDateSysMetadataModified().toGregorianCalendar()
            .getTime().after(earliest);
        } catch (Throwable e) {
          // implies missing or invalid sysmeta (exceptional!) so skip
          return false;
        }
      };
  }

  static Predicate<PersistedObject> before(final Date latest) {
    return latest == null ? alwaysTrue() : input -> {
        try {
          return input.getSysmeta().getDateSysMetadataModified().toGregorianCalendar()
            .getTime().before(latest);
        } catch (Throwable e) {
          // implies missing or invalid sysmeta (exceptional!) so skip
          return false;
        }
    };
  }

  static Predicate<PersistedObject> formatId(final String formatId) {
    return formatId == null ? alwaysTrue() : input ->  {
        try {
          return Objects.equal(formatId, input.getSysmeta().getFormatId());
        } catch (Throwable e) {
          // implies missing or invalid sysmeta (exceptional!) so skip
          return false;
        }
      };
  }

  static Predicate<PersistedObject> replicaStatus(final Boolean status, final NodeReference self) {
    return status == null ? alwaysTrue() : input -> {
        try {
          // indicated to include them or test that they aren't hosted copies
          return status || Objects.equal(self, input.getSysmeta().getAuthoritativeMemberNode());
        } catch (Throwable e) {
          // implies missing or invalid sysmeta (exceptional!) so skip
          return false;
        }
    };
  }
}
