package org.gbif.d1.mn.hadoop;

import org.gbif.d1.mn.MemberNodeApplication;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.memory.InMemoryBackend;

/**
 * The main entry point for running the member node.
 */
public final class D1MNHadoopApplication extends MemberNodeApplication<D1MNConfiguration> {

  private static final String APPLICATION_NAME = "DataONE Member Node";

  public static void main(String[] args) throws Exception {
    new D1MNHadoopApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  protected MNBackend getBackend(D1MNConfiguration d1MNConfiguration) {
    return new InMemoryBackend();
  }
}
