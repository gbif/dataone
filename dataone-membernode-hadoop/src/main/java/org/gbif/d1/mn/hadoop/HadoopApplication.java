package org.gbif.d1.mn.hadoop;

import org.gbif.d1.mn.MNApplication;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.memory.InMemoryBackend;

/**
 * The main entry point for running the member node.
 */
public final class HadoopApplication extends MNApplication<HadoopConfiguration> {

  private static final String APPLICATION_NAME = "DataONE Member Node (Hadoop)";

  public static void main(String[] args) throws Exception {
    new HadoopApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  protected MNBackend getBackend(HadoopConfiguration hadoopConfiguration) {
    return new InMemoryBackend();
  }
}
