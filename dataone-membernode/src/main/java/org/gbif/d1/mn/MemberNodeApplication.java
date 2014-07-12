package org.gbif.d1.mn;

import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.memory.InMemoryBackend;
import org.gbif.d1.mn.health.BackendHealthCheck;
import org.gbif.d1.mn.rest.MemberNodeResource;
import org.gbif.d1.mn.rest.error.DefaultExceptionMapper;
import org.gbif.d1.mn.rest.provider.SessionProvider;

import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.ext.ExceptionMapper;

import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.Service;
import org.dataone.ns.service.types.v1.Services;

/**
 * The main entry point for running the member node.
 * <p>
 * Developers are expected to inherit from this, along with a new configuration object and implement
 * 
 * @param <T> For the configuration object to support custom backends which require configuration
 */
public class MemberNodeApplication<T extends MemberNodeConfiguration> extends Application<T> {

  private static final String APPLICATION_NAME = "DataONE Member Node";

  public static void main(String[] args) throws Exception {
    new MemberNodeApplication<MemberNodeConfiguration>().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public final void initialize(Bootstrap<T> bootstrap) {
  }

  @Override
  public final void run(T configuration, Environment environment) {
    // note: the method is typically overriden by a subclass
    MNBackend backend = getBackend(configuration);

    // exception handling
    // removeDropwizardExceptionMappers(environment.jersey());
    environment.jersey().register(new DefaultExceptionMapper());

    // providers
    environment.jersey().register(SessionProvider.newWithDefaults());

    // RESTful resources
    environment.jersey().register(new MemberNodeResource(backend, thisNode(configuration)));

    // health checks
    environment.healthChecks().register("backend", new BackendHealthCheck(backend));
  }

  /**
   * Strips the default Dropwizard Exception handling and replaces with our own.
   * <p>
   * TODO: remove this?
   * 
   * @see <a href="http://thoughtspark.org/2013/02/25/dropwizard-and-jersey-exceptionmappers/">More information</a>
   */
  private void removeDropwizardExceptionMappers(JerseyEnvironment environment) {
    ResourceConfig jrConfig = environment.getResourceConfig();
    Set<Object> dwSingletons = jrConfig.getSingletons();
    Iterator<Object> iter = dwSingletons.iterator();

    // remove any of the dropwizard ExceptionMappers
    while (iter.hasNext()) {
      Object s = iter.next();
      if (s instanceof ExceptionMapper && s.getClass().getName().startsWith("io.dropwizard.jersey.")) {
        dwSingletons.remove(s);
      }
    }
  }

  /**
   * Returns a Node representing this installation, based on the configuration.
   * TODO: extract config
   */
  private Node thisNode(MemberNodeConfiguration configuration) {
    // nonsense for now
    return Node.builder()
      .withIdentifier(NodeReference.builder().withValue("urn:node:GBIFS").build())
      .withName("GBIFS Member Node")
      .withDescription("TODO")
      .withBaseURL("https://localhost:8443/d1/mn")
      .withServices(Services.builder()
        .addService(Service.builder().withAvailable(true).withName("MNCore").withVersion("v1").build())
        .addService(Service.builder().withAvailable(true).withName("MNRead").withVersion("v1").build())
        .addService(Service.builder().withAvailable(true).withName("MNAuthorization").withVersion("v1").build())
        .addService(Service.builder().withAvailable(true).withName("MNStorage").withVersion("v1").build())
        .addService(Service.builder().withAvailable(true).withName("MNReplication").withVersion("v1").build())
        .build()).build();
  }

  /**
   * Creates the backend using the configuration. Developers implementing custom backends will override this method.
   */
  protected MNBackend getBackend(T configuration) {
    return new InMemoryBackend();
  }
}
