package org.gbif.d1.mn.hadoop;

import org.gbif.d1.mn.MemberNodeConfiguration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * D1MNHadoopApplication configuration with sensible defaults if applicable.
 */
public class D1MNConfiguration extends MemberNodeConfiguration {

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