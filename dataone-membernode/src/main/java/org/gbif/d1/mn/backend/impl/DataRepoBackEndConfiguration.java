package org.gbif.d1.mn.backend.impl;

import org.gbif.d1.mn.MNConfiguration;
import org.gbif.d1.mn.Tier;
import org.gbif.datarepo.conf.DataRepoConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration settings of a backend supported on the GBIF data repo.
 */
public class DataRepoBackendConfiguration extends MNConfiguration {

  private DataRepoConfiguration dataRepoConfiguration;

  @JsonProperty
  public DataRepoConfiguration getDataRepoConfiguration() {
    return dataRepoConfiguration;
  }

  public void setDataRepoConfiguration(DataRepoConfiguration dataRepoConfiguration) {
    this.dataRepoConfiguration = dataRepoConfiguration;
  }

  /**
   * DataOne implementation Tier.
   * This service implement up to tier 4.
   */
  public Tier getTier() {
    return Tier.TIER4;
  }
}
