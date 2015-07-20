package org.gbif.d1.mn.rest;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.dataone.ns.service.apis.v1.MNReplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event bus listener that will action the asynchronous requests and perform necessary audit logging.
 */
final class EventListener {

  private static final Logger LOG = LoggerFactory.getLogger(EventListener.class);
  private final MNReplication replication;

  EventListener(EventBus eventBus, MNReplication replication) {
    eventBus.register(this);
    this.replication = replication;
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
