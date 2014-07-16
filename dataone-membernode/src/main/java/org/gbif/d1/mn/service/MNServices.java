package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import org.dataone.ns.service.apis.v1.MNAuthorization;
import org.dataone.ns.service.apis.v1.MNRead;
import org.dataone.ns.service.apis.v1.MNReplication;
import org.dataone.ns.service.apis.v1.MNStorage;
import org.dataone.ns.service.types.v1.Node;

/**
 * Static utility methods pertaining to instances of the DataONE member node implementations.
 */
public class MNServices {

  public static MNAuthorization authorizationService(Node self, AuthorizationManager authorizationManager,
    MNBackend backend) {
    return new MNAuthorizationImpl(self, authorizationManager, backend);
  }

  public static MNRead readService(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    return new MNReadImpl(self, authorizationManager, backend);
  }

  public static MNReplication replicationService(Node self, AuthorizationManager authorizationManager,
    MNBackend backend) {
    return new ReplicationService(self, authorizationManager, backend);
  }

  public static MNStorage
    storageService(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    return new StorageService(self, authorizationManager, backend);
  }
}
