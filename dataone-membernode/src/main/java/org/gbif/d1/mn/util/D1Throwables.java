package org.gbif.d1.mn.util;

import java.util.Optional;
import javax.annotation.Nullable;

import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.ServiceFailure;

/**
 * Utilities to simplify exception handling.
 */
public class D1Throwables {

  /**
   * Private constructor.
   */
  private D1Throwables() {
    //empty
  }

  private static final String DEFAULT_SERVICE_FAILURE_MESSAGE = "Unexpected service failure";

  /**
   * Propagates the exception as is if it is a DataONEException, otherwise throws a {@link ServiceFailure} with the
   * provided text or some default text if null.
   *
   * @param throwable The cause
   * @param errorMessage The optional message to use for a service failure
   */
  public static <E extends DataONEException> E propagateOrServiceFailure(@Nullable Throwable throwable,
                                                                         @Nullable String errorMessage) {
    if (throwable instanceof DataONEException) {
      throw (DataONEException) throwable;
    } else {
      throw Optional.ofNullable(throwable)
              .map(t -> new ServiceFailure(Optional.ofNullable(errorMessage).orElse(DEFAULT_SERVICE_FAILURE_MESSAGE),
                                           t))
              .orElse(new ServiceFailure(DEFAULT_SERVICE_FAILURE_MESSAGE));
    }


  }
}
