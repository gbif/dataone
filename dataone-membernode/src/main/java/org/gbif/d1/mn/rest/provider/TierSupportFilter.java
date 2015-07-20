package org.gbif.d1.mn.rest.provider;

import org.gbif.d1.mn.Tier;
import org.gbif.d1.mn.rest.exception.DataONE;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

/**
 * Checks to see if the target method is supported by the configuration.
 */
public class TierSupportFilter implements DynamicFeature {

  // the Tier at which the application is running
  private final Tier operatingLevel;

  public TierSupportFilter(Tier operatingLevel) {this.operatingLevel = operatingLevel;}

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    operatingLevel.checkIsSupported(resourceInfo.getResourceMethod().getAnnotation(DataONE.class));
  }
}
