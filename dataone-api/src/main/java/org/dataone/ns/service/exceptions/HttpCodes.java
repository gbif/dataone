package org.dataone.ns.service.exceptions;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Utilities to map exceptions to HTTP codes-
 */
public class HttpCodes {

  // Maps Exception types to HTTP codes
  private static final Map<Class<? extends DataONEException>, Integer> HTTP_CODES =
    ImmutableMap.<Class<? extends DataONEException>, Integer>builder()
      .put(AuthenticationTimeout.class, 408)
      .put(IdentifierNotUnique.class, 409)
      .put(InsufficientResources.class, 413)
      .put(InvalidCredentials.class, 401)
      .put(InvalidRequest.class, 400)
      .put(InvalidSystemMetadata.class, 400)
      .put(InvalidToken.class, 401)
      .put(NotAuthorized.class, 401)
      .put(NotFound.class, 404)
      .put(NotImplemented.class, 501)
      .put(ServiceFailure.class, 500)
      .put(UnsupportedMetadataType.class, 400)
      .put(UnsupportedType.class, 400)
      .put(SynchronizationFailed.class, 0) // TODO: this is not a valid code, but in the spec
      .put(VersionMismatch.class, 409)
      .build();

  /**
   * Returns the HTTP code to use when throwing the exception over the network.
   * 
   * @throws IllegalArgumentException if the type is not supported
   */
  public static int forType(DataONEException exception) {
    if (HTTP_CODES.containsKey(exception.getClass())) {
      return HTTP_CODES.get(exception.getClass());
    }
    throw new IllegalArgumentException("Type " + exception.getClass() + " is not supported");
  }
}
