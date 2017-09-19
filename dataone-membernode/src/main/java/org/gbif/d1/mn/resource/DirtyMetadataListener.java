package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.backend.MNBackend;

import com.google.common.eventbus.Subscribe;
import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirtyMetadataListener {

  public static class SystemMetadataChangeEvent {
    private final Identifier identifier;
    private final Session session;

    public SystemMetadataChangeEvent(Identifier identifier, Session session) {
      this.session = session;
      this.identifier = identifier;
    }

    public Identifier getIdentifier() {
      return identifier;
    }

    public Session getSession() {
      return session;
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(DirtyMetadataListener.class);

  private final CoordinatingNode coordinatingNode;
  private final MNBackend mnBackend;

  public DirtyMetadataListener(CoordinatingNode coordinatingNode, MNBackend mnBackend) {
    this.coordinatingNode = coordinatingNode;
    this.mnBackend = mnBackend;
  }

  @Subscribe public void updateMetadata(SystemMetadataChangeEvent metadataChangeEvent) {
    try {
      SystemMetadata metadata = coordinatingNode.getSystemMetadata(metadataChangeEvent.getIdentifier());
      mnBackend.updateMetadata(metadataChangeEvent.getSession(), metadataChangeEvent.getIdentifier(), metadata);
      LOG.debug("Metadata update for Identifier {}", metadataChangeEvent.getIdentifier().getValue());
    } catch (Exception ex) {
      LOG.error("Error updating metadata", ex);
    }
  }
}
