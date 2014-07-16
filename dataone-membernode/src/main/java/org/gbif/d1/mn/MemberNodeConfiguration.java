package org.gbif.d1.mn;

import io.dropwizard.Configuration;

/**
 * Application configuration with sensible defaults if applicable.
 */
public class MemberNodeConfiguration extends Configuration {

  public enum TIER {
    TIER1, TIER2, TIER3, TIER4
  }
}