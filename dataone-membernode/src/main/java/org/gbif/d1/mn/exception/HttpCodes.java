package org.gbif.d1.mn.exception;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.dataone.ns.service.exceptions.AuthenticationTimeout;
import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidCredentials;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.SynchronizationFailed;
import org.dataone.ns.service.exceptions.UnsupportedMetadataType;
import org.dataone.ns.service.exceptions.UnsupportedType;
import org.dataone.ns.service.exceptions.VersionMismatch;

/**
 * HTTP codes for raised exceptions.
 */
class HttpCodes {

  private static final Map<Class<? extends DataONEException>, Integer> HTTP_CODES = ImmutableMap
    .<Class<? extends DataONEException>, Integer>builder()
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
    .put(VersionMismatch.class, 409)
    .build();

  /**
   * @return the detail code to use for the exception within the exception, or null if not found.
   */
  static Integer codeFor(Class<? extends DataONEException> exception) {
    return HTTP_CODES.get(exception);
  }
}
