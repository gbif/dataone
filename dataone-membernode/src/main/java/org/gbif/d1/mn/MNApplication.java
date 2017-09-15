package org.gbif.d1.mn;

import org.gbif.d1.cn.impl.CNClient;
import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.auth.AuthorizationManagers;
import org.gbif.d1.mn.auth.CertificateUtils;
import org.gbif.d1.mn.backend.BackendHealthCheck;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.impl.DataRepoBackend;
import org.gbif.d1.mn.backend.impl.DataRepoBackendConfiguration;
import org.gbif.d1.mn.exception.DefaultExceptionMapper;
import org.gbif.d1.mn.logging.impl.ElasticsearchLogSearchService;
import org.gbif.d1.mn.provider.DescribeResponseHeaderProvider;
import org.gbif.d1.mn.provider.EventProvider;
import org.gbif.d1.mn.provider.IdentifierProvider;
import org.gbif.d1.mn.provider.SessionProvider;
import org.gbif.d1.mn.provider.TierSupportFilter;
import org.gbif.d1.mn.resource.ArchiveResource;
import org.gbif.d1.mn.resource.CapabilitiesResource;
import org.gbif.d1.mn.resource.ChecksumResource;
import org.gbif.d1.mn.resource.ErrorResource;
import org.gbif.d1.mn.resource.GenerateResource;
import org.gbif.d1.mn.resource.LogResource;
import org.gbif.d1.mn.resource.MetaResource;
import org.gbif.d1.mn.resource.MonitorResource;
import org.gbif.d1.mn.resource.ObjectResource;
import org.gbif.d1.mn.resource.ReplicaResource;
import org.gbif.datarepo.inject.DataRepoFsModule;
import org.gbif.datarepo.store.fs.conf.DataRepoConfiguration;
import org.gbif.discovery.lifecycle.DiscoveryLifeCycle;


import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.Service;
import org.dataone.ns.service.types.v1.Services;
import org.dataone.ns.service.types.v1.Subject;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for running the member node.
 * <p>
 * Developers are expected to inherit from this, along with a new configuration object and implement
 */
public class MNApplication extends Application<DataRepoBackendConfiguration> {

  private static final Logger LOG = LoggerFactory.getLogger(MNApplication.class);

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
    //Can be discovered in zookeeper
    if (configuration.getService().isDiscoverable()) {
      environment.lifecycle().manage(new DiscoveryLifeCycle(configuration.getService()));
    }


    Node self = self(configuration);
    CertificateUtils certificateUtils = CertificateUtils.newInstance();
    environment.getObjectMapper().registerModules(new JaxbAnnotationModule());
    // Replace all exception handling with custom handling required by the DataONE specification
    ((AbstractServerFactory)configuration.getServerFactory()).setRegisterDefaultExceptionMappers(false);
    environment.jersey().register(new DefaultExceptionMapper(self.getIdentifier().getValue()));

    environment.jersey().register(new LoggingFilter(java.util.logging.Logger.getLogger("InboundRequestResponse"), true));

    // providers
    // TODO: read config here to support overwriting OIDs in certificates
    environment.jersey().register(new SessionProvider(certificateUtils));
    environment.jersey().register(new TierSupportFilter(configuration.getTier()));
    environment.jersey().register(new IdentifierProvider());
    environment.jersey().register(new DescribeResponseHeaderProvider());
    environment.jersey().register(new EventProvider());

    // RESTful resources
    CoordinatingNode cn = coordinatingNode(configuration, environment);
    MNBackend backend = getBackend(configuration, environment);
    AuthorizationManager auth = AuthorizationManagers.newAuthorizationManager(backend, cn, self);
    environment.jersey().register(new CapabilitiesResource(self, certificateUtils));
    environment.jersey().register(new ArchiveResource(auth, backend));
    environment.jersey().register(new ObjectResource(auth, backend));
    environment.jersey().register(new MetaResource(auth, backend));
    environment.jersey().register(new ChecksumResource(auth, backend));
    environment.jersey().register(new GenerateResource(auth, backend));
    environment.jersey().register(new MonitorResource(backend));
    environment.jersey().register(new LogResource(new ElasticsearchLogSearchService(configuration.getElasticSearch().buildEsClient(),
                                                  configuration.getElasticSearch().getIdx()), auth));
    environment.jersey().register(new ErrorResource(auth));
    environment.jersey().register(new ReplicaResource(backend, auth));

    // health checks
    environment.healthChecks().register("backend", new BackendHealthCheck(backend));
  }

  private static CoordinatingNode coordinatingNode(MNConfiguration configuration, Environment environment) {
    return new CNClient(new JerseyClientBuilder(environment).build("CNClient"), configuration.getCoordinatingNodeUrl());
  }

  /**
   * Returns a Node representing this installation, based on the provided configuration.
   * TODO: extract config
   */
  private static Node self(DataRepoBackendConfiguration configuration) {
    // nonsense for now
    return Node.builder()
      .addSubject(Subject.builder().withValue("CN=GBIFS Test Member Node").build())
      .withIdentifier(NodeReference.builder().withValue("urn:node:mnTestGBIF").build())
      .withName("GBIFS Member Node")
      .withDescription("GBIFS DataOne Member Node")
      .withBaseURL(configuration.getExternalUrl())
      .withContactSubject(Subject.builder().withValue("CN=Informatics, DC=GBIFS, DC=org").build())
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
  protected static MNBackend getBackend(DataRepoBackendConfiguration configuration, Environment environment) {
    DataRepoConfiguration dataRepoConfiguration = configuration.getDataRepoConfiguration();
    DataRepoFsModule dataRepoModule = new DataRepoFsModule(dataRepoConfiguration, environment.metrics(),
                                                           environment.healthChecks());
    return new DataRepoBackend(dataRepoModule.dataRepository(environment.getObjectMapper()),
                               dataRepoModule.doiRegistrationService(environment.getObjectMapper()),
                               configuration);
  }
}
