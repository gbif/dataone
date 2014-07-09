package org.gbif.d1.mn;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendConfiguration {

  @Min(1)
  @Max(120)
  private int age;

  @JsonProperty("age")
  public int getAge() {
    return age;
  }

  @JsonProperty("age")
  public void setAge(int age) {
    this.age = age;
  }
}
