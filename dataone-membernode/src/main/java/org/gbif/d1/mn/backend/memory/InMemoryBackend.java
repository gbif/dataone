package org.gbif.d1.mn.backend.memory;

import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.model.MNLogEntry;

import java.io.InputStream;
import java.security.Principal;
import java.util.List;

import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Work in progress. IGNORE THIS CLASS
 */
public class InMemoryBackend implements MNBackend {

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public void create(Principal userPrincipal, String pid, InputStream object, SystemMetadata sysmeta) {
    // TODO Auto-generated method stub

  }

  @Override
  public InputStream get(Principal userPrincipal, String pid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<MNLogEntry> getLogs() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Principal getOwner(String pid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SystemMetadata getSystemMetadata(String identifier) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Health health() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String log(MNLogEntry logEntry) {
    // TODO Auto-generated method stub
    return null;
  }

}
