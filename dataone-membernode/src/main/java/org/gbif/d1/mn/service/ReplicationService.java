package org.gbif.d1.mn.service;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.backend.MNBackend;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import org.dataone.ns.service.apis.v1.MNReplication;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Tier 4 of the MN stack.
 * <p>
 * It is a requirement that this be constructed with thread-safe {@link Node}, {@link AuthorizationManager} and
 * {@link MNBackend}, which will make this class unconditionally thread-safe.
 * <p>
 * Not designed for further inheritance.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">DataONE Member Node API</a>
 */
final class ReplicationService implements MNReplication {

  private static final Logger LOG = LoggerFactory.getLogger(ReplicationService.class);

  private final AuthorizationManager authorizationManager;
  private final EventBus eventBus;

  public ReplicationService(AuthorizationManager authorizationManager, EventBus eventBus) {
    this.authorizationManager = authorizationManager;
    this.eventBus = eventBus;
  }

  @Override
  public boolean replicate(Session session, SystemMetadata sysmeta, String sourceNode) {
    Preconditions.checkNotNull(sysmeta, "System metadata is required");
    Preconditions.checkNotNull(sysmeta.getIdentifier(), "Identifier is required in the system metadata");
    Preconditions.checkNotNull(sysmeta.getIdentifier().getValue(), "Identifier is required in the system metadata");
    Preconditions.checkNotNull(sourceNode, "SourceNode is required");

    // TODO: what about authorization?
    // TODO: what do we do for the sourceNode stuff?
    eventBus.post(new ReplicateEvent(sysmeta.getIdentifier().getValue(), sourceNode));
    return true;
  }
}
