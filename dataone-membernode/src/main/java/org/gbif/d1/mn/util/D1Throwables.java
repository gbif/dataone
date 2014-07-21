package org.gbif.d1.mn.util;

import javax.annotation.Nullable;

import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.ServiceFailure;

/**
 * Utilities to simplify exception handling.
 */
public class D1Throwables {

  private static final String DEFAULT_SERVICE_FAILURE_MESSAGE = "Unexpected service failure";

  /**
   * Propagtes the exception as is if it is a DataONEException, otherwise throws a {@link ServiceFailure} with the
   * provided text or some default text if null.
   * 
   * @param throwable The cause
   * @param errorMessage The optional message to use for a service failure
   */
  public static <E extends DataONEException> E propagateOrServiceFailure(@Nullable Throwable throwable,
    @Nullable String errorMessage) {
    if (throwable != null && throwable instanceof DataONEException) {
      throw (DataONEException) throwable;
    } else if (throwable != null) {
      throw new ServiceFailure(
        errorMessage == null ? DEFAULT_SERVICE_FAILURE_MESSAGE : errorMessage,
        throwable);
    } else {
      throw new ServiceFailure(DEFAULT_SERVICE_FAILURE_MESSAGE);
    }
  }
}
