package org.gbif.d1.mn.rest;

import javax.annotation.Nullable;

import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.NotImplemented;

/**
 * Utilities for preconditions specific to the DataONE REST layer.
 */
final class D1Preconditions {

  // not for instantiation
  private D1Preconditions() {
  }

  /**
   * Ensure that the provided object reference is not null, indicating that it is supported by this configuration.
   * 
   * @param reference an object reference
   * @return the non-null reference that was validated
   * @throws NotImplemented If {@code reference} is null
   */
  static <T> T checkIsSupported(T reference) {
    if (reference == null) {
      throw new NotImplemented("This node is not configured to support the operation");
    }
    return reference;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   * 
   * @param reference an object reference
   * @param errorMessage the exception message to use if the check fails
   * @return the non-null reference that was validated
   * @throws InvalidRequest if {@code reference} is null
   */
  static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }
}
