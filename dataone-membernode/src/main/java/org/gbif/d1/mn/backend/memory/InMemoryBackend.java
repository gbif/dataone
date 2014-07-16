package org.gbif.d1.mn.backend.memory;

import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;

import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * An in-memory implementation of the back-end, suitable for testing only.
 */
public class InMemoryBackend implements MNBackend {

  @Override
  public void close() {
  }

  @Override
  public SystemMetadata getSystemMetadata(Session session, String identifier) {
    return null;
  }

  /**
   * Always healthy if we can answer this call.
   */
  @Override
  public Health health() {
    return Health.healthy();
  }
}
