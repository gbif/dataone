package org.gbif.d1.mn.provider;

import org.gbif.d1.mn.Tier;
import org.gbif.d1.mn.exception.DataONE;

import java.util.Optional;
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
    // Only methods that are annotated with DataONE annotations are checked, as there are
    // methods that we wish to skip (such as those that can provide WSDL or Dropwizards GC resources)
    Optional.ofNullable(resourceInfo.getResourceMethod().getAnnotation(DataONE.class))
      .ifPresent(annotation -> operatingLevel.checkIsSupported(resourceInfo.getResourceMethod()
                                                                 .getAnnotation(DataONE.class)));

  }
}
