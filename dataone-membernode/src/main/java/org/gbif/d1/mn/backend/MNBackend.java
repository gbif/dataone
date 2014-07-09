package org.gbif.d1.mn.backend;

import org.gbif.d1.mn.backend.model.MNLogEntry;

import java.io.InputStream;
import java.security.Principal;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.dataone.ns.service.apis.v1.SystemMetadataProvider;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * The contract that a backend must adhere to, allowing it to plug in to the web layer.
 * All implementations are <strong>required</strong> to be fully threadsafe.
 * <p>
 * TODO: Ignore the methods here... this is a work in progress
 */
public interface MNBackend extends SystemMetadataProvider {

  /**
   * Indicates the backend can be closed which might close resources, and flush caches.
   * Once closed, the backend will not be reopened.
   */
  void close();

  void create(Principal userPrincipal, String pid, InputStream object, SystemMetadata sysmeta);

  /**
   * @param userPrincipal Provided to access the item
   * @param pid The identifier
   * @return A stream to the digital object
   */
  InputStream get(Principal userPrincipal, String pid);

  List<MNLogEntry> getLogs();

  /**
   * Returns the subject associated with the identified object.
   */
  @NotNull
  Principal getOwner(@NotNull String pid);

  /**
   * A health check to assist in monitoring.
   */
  Health health();

  String log(MNLogEntry logEntry);
}