package org.gbif.d1.mn;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.auth.AuthorizationManagers;
import org.gbif.d1.mn.backend.BackendHealthCheck;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.memory.InMemoryBackend;
import org.gbif.d1.mn.rest.MemberNodeResource;
import org.gbif.d1.mn.rest.exception.DefaultExceptionMapper;
import org.gbif.d1.mn.rest.provider.SessionProvider;
import org.gbif.d1.mn.service.MNServices;

import java.util.Set;

import javax.ws.rs.ext.ExceptionMapper;

import com.google.common.collect.Sets;
import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeList;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.Service;
import org.dataone.ns.service.types.v1.Services;
import org.dataone.ns.service.types.v1.Subject;

/**
 * The main entry point for running the member node.
 * <p>
 * Developers are expected to inherit from this, along with a new configuration object and implement
 * 
 * @param <T> For the configuration object to support custom backends which require configuration
 */
public class MNApplication<T extends MNConfiguration> extends Application<T> {

  private static final String APPLICATION_NAME = "DataONE Member Node";

  public static void main(String[] args) throws Exception {
    new MNApplication<MNConfiguration>().run(args);
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
    Node self = self(configuration);

    // exception handling removes defaults, and adds custom handling
    removeAllExceptionMappers(environment.jersey());
    environment.jersey().register(new DefaultExceptionMapper(self.getIdentifier().getValue()));

    // providers
    // TODO: read config here to support overwriting OIDs in certificates
    environment.jersey().register(SessionProvider.newWithDefaults());

    // RESTful resources
    CoordinatingNode cn = coordinatingNode(configuration);
    MNBackend backend = getBackend(configuration);
    AuthorizationManager auth = AuthorizationManagers.newAuthorizationManager(backend, cn, self);
    environment.jersey().register(new MemberNodeResource(
      MNServices.readService(self, auth, backend),
      MNServices.authorizationService(auth),
      MNServices.storageService(auth, backend),
      MNServices.replicationService(auth)));

    // health checks
    environment.healthChecks().register("backend", new BackendHealthCheck(backend));
  }

  // TODO: implement a CN client
  private CoordinatingNode coordinatingNode(MNConfiguration configuration) {
    return new CoordinatingNode() {

      @Override
      public NodeList listNodes() throws ServiceFailure {
        return NodeList.builder().build();
      }
    };
  }

  /**
   * Removes all instances of {@link ExceptionMapper} from the environment.
   */
  private void removeAllExceptionMappers(JerseyEnvironment environment) {
    ResourceConfig jrConfig = environment.getResourceConfig();
    Set<Object> dwSingletons = jrConfig.getSingletons();
    Set<Object> keysToRemove = Sets.newHashSet();
    for (Object s : jrConfig.getSingletons()) {
      if (s instanceof ExceptionMapper) {
        keysToRemove.add(s);
      }
    }
    for (Object s : keysToRemove) {
      dwSingletons.remove(s);
    }
  }

  /**
   * Returns a Node representing this installation, based on the provided configuration.
   * TODO: extract config
   */
  private Node self(MNConfiguration configuration) {
    // nonsense for now
    return Node.builder()
      .addSubject(Subject.builder().withValue("CN=GBIFS Member Node").build())
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
