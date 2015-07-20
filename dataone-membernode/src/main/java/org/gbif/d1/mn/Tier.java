package org.gbif.d1.mn;

import org.gbif.d1.mn.rest.exception.DataONE;
import static org.gbif.d1.mn.rest.exception.DataONE.Method.*;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

/**
 * An enumeration mapping the methods per DataONE tier.
 * <p>
 * This class exists to simplify the checking of whether an invoked method is permissible in the running configuration.
 */
public enum Tier {
  // tiers extend each other
  TIER1(PING, GET_LOG_RECORDS, GET_CAPABILITIES, GET, GET_SYSTEM_METADATA, DESCRIBE, GET_CHECKSUM, LIST_OBJECTS,
        SYNCHRONIZATION_FAILED, SYSTEM_METADATA_CHANGED, GET_REPLICA, UPDATE_SYSTEM_METADATA, QUERY,
        GET_QUERY_ENGINE_DESCRIPTION, VIEW, LIST_VIEWS, GET_PACKAGE),
  TIER2(TIER1, IS_AUTHORIZED),
  TIER3(TIER2, CREATE, UPDATE, GENERATE_IDENTIFIER, DELETE, ARCHIVE),
  TIER4(TIER3, REPLICATE);

  private final Set<DataONE.Method> supportedMethods;

  Tier(DataONE.Method... methods) {
    supportedMethods = ImmutableSet.<DataONE.Method>builder().add(methods).build();
  }

  Tier(Tier tier, DataONE.Method... methods) {
    supportedMethods = ImmutableSet.<DataONE.Method>builder().addAll(tier.supportedMethods).add(methods).build();
  }

  /**
   * Verifies the given method is associated with the tier otherwise throws ISE.
   * @param annotation containing the method to check existence of within the tier
   * @throws IllegalStateException Should the method be null or not associated with the tier
   */
  public void checkIsSupported(@Nullable DataONE annotation) throws IllegalStateException {
    if (annotation == null || annotation.value() == null || !supportedMethods.contains(annotation.value())) {
      throw new IllegalStateException("The method is not supported by the tier " + name());
    }
  }
}
