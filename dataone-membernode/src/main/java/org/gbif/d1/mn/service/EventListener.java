package org.gbif.d1.mn.service;

import org.gbif.d1.mn.backend.MNBackend;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event bus listener that will call the CN to retrieve the authoritative system metadata and update our copy.
 */
final class EventListener {

  private static final Logger LOG = LoggerFactory.getLogger(EventListener.class);
  private final CoordinatingNode cn;
  private final MNBackend backend;

  EventListener(EventBus eventBus, CoordinatingNode cn, MNBackend backend) {
    eventBus.register(this);
    this.cn = cn;
    this.backend = backend;
  }

  @Subscribe
  final void replicate(ReplicateEvent event) {
    LOG.info("Received notification to replicate pid[{}] from sourceNode[{}]", event.getIdentifier(),
      event.getSourceNode());
  }

  @Subscribe
  final void systemMetadataUpdate(SystemMetadataUpdateEvent event) {
    LOG.info("Received notification that system metadata changed for: {}", event.getIdentifier());
    // TODO:
    // ask if we should schedule a delete if the replication rules of the object mean we should not even have a copy.
  }
}
