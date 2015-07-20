package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import com.google.common.eventbus.EventBus;
import org.dataone.ns.service.apis.v1.MNAuthorization;
import org.dataone.ns.service.apis.v1.MNRead;
import org.dataone.ns.service.apis.v1.MNReplication;
import org.dataone.ns.service.apis.v1.MNStorage;
import org.dataone.ns.service.types.v1.Node;

/**
 * Static utility methods pertaining to instances of the DataONE member node implementations.
 */
public class MNServices {

  public static MNAuthorization authorizationService(AuthorizationManager auth) {
    return new MNAuthorizationImpl(auth);
  }

  public static MNRead readService(Node self, AuthorizationManager auth, MNBackend backend, EventBus eventBus) {
    return new MNReadImpl(self, auth, backend, eventBus);
  }

  public static MNReplication replicationService(AuthorizationManager auth, EventBus eventBus) {
    return new ReplicationService(auth, eventBus);
  }

  public static MNStorage storageService(AuthorizationManager auth, MNBackend backend) {
    return new StorageService(auth, backend);
  }
}
