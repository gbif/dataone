package org.gbif.d1.mn.backend;

import org.dataone.ns.service.apis.v1.SystemMetadataProvider;

/**
 * The contract that a back-end must adhere to, allowing it to plug in to the REST layer.
 * <p>
 * <strong>All implementations are required to be fully threadsafe.</strong>
 */
public interface MNBackend extends SystemMetadataProvider {

  /**
   * Indicates the back-end can be closed which might close resources, and flush caches.
   * Once closed, the back-end will not be reopened.
   */
  void close();

  /**
   * A health test for the back-end. The back-end should perform some minimum test to ensure that it is operational.
   * This is may be called frequently so should be a very high speed operation. A sample SQL call, or verification that
   * a file system can be read would be examples of suitable implementations.
   * 
   * @return the result of the health test
   */
  Health health();
}