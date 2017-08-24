package org.gbif.d1.mn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

/**
 * Application configuration with sensible defaults if applicable.
 */
public class MNConfiguration extends Configuration {

  public enum TIER {
    TIER1, TIER2, TIER3, TIER4
  }

  private String externalUrl;

  private String coordinatingNodeUrl;

  public Tier getTier(){
    return Tier.TIER4;
  }

  /**
   * External visible URL of this service.
   */
  public String getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  @JsonProperty
  public String getCoordinatingNodeUrl() {
    return coordinatingNodeUrl;
  }

  public void setCoordinatingNodeUrl(String coordinatingNodeUrl) {
    this.coordinatingNodeUrl = coordinatingNodeUrl;
  }
}
