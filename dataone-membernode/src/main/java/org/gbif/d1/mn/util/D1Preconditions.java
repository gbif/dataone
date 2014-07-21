package org.gbif.d1.mn.util;

import javax.annotation.Nullable;

import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.types.v1.Identifier;

/**
 * Utilities for preconditions specific to the DataONE REST layer.
 */
public final class D1Preconditions {

  // not for instantiation
  private D1Preconditions() {
  }

  /**
   * Ensures that an object reference passed as a parameter is not null.
   * 
   * @param reference an object reference
   * @param identifier the unique DataONE identifier for the object
   * @param message the exception message to use if the check fails
   * @return the non-null reference that was validated
   * @throws NotFound if {@code reference} is null
   */
  public static <T> T checkFound(T reference, Identifier identifier, String errorMessage) {
    if (reference == null) {
      throw new NotFound(errorMessage, identifier.getValue());
    } else {
      return reference;
    }
  }

  /**
   * Ensure that the provided object reference is not null, indicating that it is supported by this configuration.
   * 
   * @param reference an object reference
   * @return the non-null reference that was validated
   * @throws NotImplemented If {@code reference} is null
   */
  public static <T> T checkIsSupported(T reference) {
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
  public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
    if (reference == null) {
      throw new InvalidRequest(String.valueOf(errorMessage));
    }
    return reference;
  }

  /**
   * Ensures the truth of an expression.
   * 
   * @param expression a boolean expression
   * @param errorMessage the exception message to use if the check fails; will be converted to a
   *        string using {@link String#valueOf(Object)}
   * @throws InvalidRequest if {@code expression} is false
   */
  public static void checkState(boolean expression, @Nullable Object errorMessage) {
    if (!expression) {
      throw new InvalidRequest(String.valueOf(errorMessage));
    }
  }
}
