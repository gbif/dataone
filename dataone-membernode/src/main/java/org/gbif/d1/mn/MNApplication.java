package org.gbif.d1.mn;

import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.auth.AuthorizationManagers;
import org.gbif.d1.mn.auth.CertificateUtils;
import org.gbif.d1.mn.backend.BackendHealthCheck;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.impl.DataRepoBackend;
import org.gbif.d1.mn.backend.impl.DataRepoBackendConfiguration;
import org.gbif.d1.mn.exception.DefaultExceptionMapper;
import org.gbif.d1.mn.provider.IdentifierProvider;
import org.gbif.d1.mn.provider.SessionProvider;
import org.gbif.d1.mn.provider.TierSupportFilter;
import org.gbif.d1.mn.resource.ArchiveResource;
import org.gbif.d1.mn.resource.ChecksumResource;
import org.gbif.d1.mn.resource.MetaResource;
import org.gbif.d1.mn.resource.ObjectResource;
import org.gbif.datarepo.conf.DataRepoConfiguration;
import org.gbif.datarepo.conf.DataRepoModule;

import java.util.Set;
import javax.ws.rs.ext.ExceptionMapper;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jersey.DropwizardResourceConfig;
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
 */
public class MNApplication extends Application<DataRepoBackendConfiguration> {

  private static final String APPLICATION_NAME = "DataONE Member Node";

  public static void main(String[] args) throws Exception {
    new MNApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public final void initialize(Bootstrap<DataRepoBackendConfiguration> bootstrap) {
    bootstrap.addBundle(new MultiPartBundle());
  }

  @Override
  public final void run(DataRepoBackendConfiguration configuration, Environment environment) {
    Node self = self(configuration);

    // Replace all exception handling with custom handling required by the DataONE specification
    removeAllExceptionMappers(environment.jersey());
    environment.jersey().register(new DefaultExceptionMapper(self.getIdentifier().getValue()));

    // providers
    // TODO: read config here to support overwriting OIDs in certificates
    environment.jersey().register(new SessionProvider(CertificateUtils.newInstance()));
    environment.jersey().register(new TierSupportFilter(configuration.getTier()));
    environment.jersey().register(new IdentifierProvider());

    // RESTful resources
    CoordinatingNode cn = coordinatingNode(configuration);
    MNBackend backend = getBackend(configuration, environment);
    AuthorizationManager auth = AuthorizationManagers.newAuthorizationManager(backend, cn, self);
    environment.jersey().register(new ArchiveResource(auth));
    environment.jersey().register(new ObjectResource(auth, backend));
    environment.jersey().register(new MetaResource(auth, backend));
    environment.jersey().register(new ChecksumResource(auth, backend));

    // health checks
    environment.healthChecks().register("backend", new BackendHealthCheck(backend));
  }

  // TODO: implement a CN client
  private static CoordinatingNode coordinatingNode(MNConfiguration configuration) {
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
  private static void removeAllExceptionMappers(JerseyEnvironment environment) {
    DropwizardResourceConfig jrConfig = environment.getResourceConfig();
    Set<Object> dwSingletons = jrConfig.getSingletons();
    jrConfig.getSingletons().stream()
      .filter(s -> s instanceof  ExceptionMapper)
      .forEach(dwSingletons::remove);
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
  protected MNBackend getBackend(DataRepoBackendConfiguration configuration, Environment environment) {
    DataRepoConfiguration dataRepoConfiguration = configuration.getDataRepoConfiguration();
    DataRepoModule dataRepoModule = new DataRepoModule(dataRepoConfiguration, environment);
    return new DataRepoBackend(dataRepoModule.dataRepository(), dataRepoModule.doiRegistrationService(),
                               dataRepoConfiguration.getDoiCommonPrefix());
  }
}
