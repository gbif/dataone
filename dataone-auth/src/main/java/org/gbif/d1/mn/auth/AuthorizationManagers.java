package org.gbif.d1.mn.auth;

import java.util.List;

import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.apis.v1.SystemMetadataProvider;
import org.dataone.ns.service.types.v1.Node;

/**
 * Factories for creating instances of {@link AuthorizationManager}.
 */
public final class AuthorizationManagers {

  public static AuthorizationManager
    newAuthorizationManager(SystemMetadataProvider systemMetadataProvider, CoordinatingNode cn, Node self) {
    return new AuthorizationManagerImpl(systemMetadataProvider, cn, self);
  }

  public static AuthorizationManager newAuthorizationManager(SystemMetadataProvider systemMetadataProvider,
    CoordinatingNode cn, Node self, List<String> subjectInfoExtensionOIDs) {
    return new AuthorizationManagerImpl(systemMetadataProvider, cn, self, subjectInfoExtensionOIDs);
  }
}
