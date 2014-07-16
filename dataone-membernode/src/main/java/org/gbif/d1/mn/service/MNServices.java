package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import org.dataone.ns.service.apis.v1.MemberNodeAuthorization;
import org.dataone.ns.service.apis.v1.MemberNodeRead;
import org.dataone.ns.service.apis.v1.MemberNodeReplication;
import org.dataone.ns.service.apis.v1.MemberNodeStorage;
import org.dataone.ns.service.types.v1.Node;

/**
 * Static utility methods pertaining to instances of the DataONE member node implementations.
 */
public class MNServices {

  public static MemberNodeAuthorization authorizationService(Node self, AuthorizationManager authorizationManager,
    MNBackend backend) {
    return new AuthorizationService(self, authorizationManager, backend);
  }

  public static MemberNodeRead readService(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    return new ReadService(self, authorizationManager, backend);
  }

  public static MemberNodeReplication replicationService(Node self, AuthorizationManager authorizationManager,
    MNBackend backend) {
    return new ReplicationService(self, authorizationManager, backend);
  }

  public static MemberNodeStorage
    storageService(Node self, AuthorizationManager authorizationManager, MNBackend backend) {
    return new StorageService(self, authorizationManager, backend);
  }
}
