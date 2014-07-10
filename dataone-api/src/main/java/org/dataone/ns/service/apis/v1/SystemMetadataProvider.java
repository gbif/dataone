package org.dataone.ns.service.apis.v1;

import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Implementations of this provide access to system metadata.
 * <p>
 * This exists in a separate interface to improve decoupling. One can write components knowing that they can access
 * system metadata without having to know about all complex services available. The authorization is one example of this
 * as system metadata informs authorization rules.
 * <p>
 * It is a <strong>requirement</strong> that implementations of this be thread-safe.
 */
public interface SystemMetadataProvider {

  /**
   * Describes the object identified by id by returning the associated system metadata object.
   */
  SystemMetadata getSystemMetadata(String identifier) throws NotAuthorized, NotFound, ServiceFailure,
    InvalidToken, InsufficientResources;

}
