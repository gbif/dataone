package org.gbif.d1.mn.hadoop;

import org.gbif.d1.mn.MNConfiguration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HadoopApplication configuration with sensible defaults if applicable.
 */
public class HadoopConfiguration extends MNConfiguration {

  @Min(1)
  @Max(65535)
  private int port = 5672;

  @JsonProperty
  public int getPort() {
    return port;
  }

  @JsonProperty
  public void setPort(int port) {
    this.port = port;
  }

}