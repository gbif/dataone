package org.gbif.d1.mn.backend.memory;

import java.util.Date;

import javax.annotation.concurrent.Immutable;

import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * An instance of this represents a persisted object in the member node.
 */
@Immutable
class PersistedObject {

  private final byte[] data;
  private final SystemMetadata sysmeta;
  private final Date created;
  private final Date modified;

  public PersistedObject(byte[] data, SystemMetadata sysmeta, Date created, Date modified) {
    this.data = data;
    this.sysmeta = sysmeta;
    this.created = created;
    this.modified = modified;
  }

  public Date getCreated() {
    return created;
  }

  public byte[] getData() {
    return data;
  }

  public Date getModified() {
    return modified;
  }

  public SystemMetadata getSysmeta() {
    return sysmeta;
  }
}
