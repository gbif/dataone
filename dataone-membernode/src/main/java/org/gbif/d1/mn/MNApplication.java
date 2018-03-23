package org.gbif.d1.mn;

import org.gbif.d1.cn.client.CNClient;
import org.gbif.d1.mn.auth.AuthorizationManager;
import org.gbif.d1.mn.auth.AuthorizationManagers;
import org.gbif.d1.mn.auth.CertificateUtils;
import org.gbif.d1.mn.backend.BackendHealthCheck;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.impl.DataRepoBackend;
import org.gbif.d1.mn.backend.impl.DataRepoBackendConfiguration;
import org.gbif.d1.mn.exception.DefaultExceptionMapper;
import org.gbif.d1.mn.logging.impl.LogbackDBLogSearchService;
import org.gbif.d1.mn.provider.DescribeResponseHeaderProvider;
import org.gbif.d1.mn.provider.EventProvider;
import org.gbif.d1.mn.provider.IdentifierProvider;
import org.gbif.d1.mn.provider.PermissionProvider;
import org.gbif.d1.mn.provider.SessionProvider;
import org.gbif.d1.mn.provider.TierSupportFilter;
import org.gbif.d1.mn.resource.ArchiveResource;
import org.gbif.d1.mn.resource.CapabilitiesResource;
import org.gbif.d1.mn.resource.ChecksumResource;
import org.gbif.d1.mn.resource.DirtyMetadataListener;
import org.gbif.d1.mn.resource.DirtySystemMetadataResource;
import org.gbif.d1.mn.resource.ErrorResource;
import org.gbif.d1.mn.resource.GenerateResource;
import org.gbif.d1.mn.resource.IsAuthorizedResource;
import org.gbif.d1.mn.resource.LogResource;
import org.gbif.d1.mn.resource.MetaResource;
import org.gbif.d1.mn.resource.MonitorResource;
import org.gbif.d1.mn.resource.ObjectResource;
import org.gbif.d1.mn.resource.ReplicaResource;
import org.gbif.d1.mn.resource.ReplicateResource;
import org.gbif.datarepo.inject.DataRepoFsModule;
import org.gbif.datarepo.impl.conf.DataRepoConfiguration;
import org.gbif.discovery.lifecycle.DiscoveryLifeCycle;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.eventbus.EventBus;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.dataone.ns.service.apis.v1.cn.CoordinatingNode;
import org.dataone.ns.service.types.v1.Node;

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
    //Can be discovered in zookeeper
    if (configuration.getService().isDiscoverable()) {
      environment.lifecycle().manage(new DiscoveryLifeCycle(configuration.getService()));
    }
    EventBus metadataChangeBus = new EventBus();

    Node self = configuration.getNode();
    CertificateUtils certificateUtils = CertificateUtils.newInstance(configuration.getTrustedOIDs());

    //JAXB binding module
    environment.getObjectMapper().registerModules(new JaxbAnnotationModule());

    // Replace all exception handling with custom handling required by the DataONE specification
    ((AbstractServerFactory)configuration.getServerFactory()).setRegisterDefaultExceptionMappers(false);
    environment.jersey().register(new DefaultExceptionMapper(self.getIdentifier().getValue()));

    // providers
    environment.jersey().register(new SessionProvider(certificateUtils));
    environment.jersey().register(new TierSupportFilter(configuration.getTier()));
    environment.jersey().register(new IdentifierProvider());
    environment.jersey().register(new DescribeResponseHeaderProvider());
    environment.jersey().register(new EventProvider());
    environment.jersey().register(new PermissionProvider());

    //Coordinating Node client
    CoordinatingNode cn = coordinatingNode(configuration, environment);

    DataRepoFsModule dataRepoFsModule = getDataRepoFsModule(configuration, environment);

    MNBackend backend = getBackend(configuration, environment, dataRepoFsModule);
    AuthorizationManager auth = AuthorizationManagers.newAuthorizationManager(backend, cn, self);

    // RESTful resources
    environment.jersey().register(new CapabilitiesResource(self, certificateUtils));
    environment.jersey().register(new ArchiveResource(auth, backend));
    environment.jersey().register(new ObjectResource(auth, backend));
    environment.jersey().register(new MetaResource(auth, backend));
    environment.jersey().register(new ChecksumResource(auth, backend));
    environment.jersey().register(new GenerateResource(auth, backend));
    environment.jersey().register(new MonitorResource(backend));
    environment.jersey().register(new LogResource(new LogbackDBLogSearchService(dataRepoFsModule.loggingMapper()),
                                                  auth));
    environment.jersey().register(new ErrorResource(auth));
    environment.jersey().register(new ReplicaResource(backend, auth, cn));
    environment.jersey().register(new IsAuthorizedResource(auth, backend));

    metadataChangeBus.register(new DirtyMetadataListener(cn, backend));
    environment.jersey().register(new DirtySystemMetadataResource(metadataChangeBus, auth));
    environment.jersey().register(new ReplicateResource(metadataChangeBus, new JerseyClientBuilder(environment), cn,
                                                        auth, backend));

    // health checks
    environment.healthChecks().register("backend", new BackendHealthCheck(backend));
  }

  private static CoordinatingNode coordinatingNode(MNConfiguration configuration, Environment environment) {
    return new CNClient(new JerseyClientBuilder(environment).using(configuration.getJerseyClient())
                          .build("CNClient"), configuration.getCoordinatingNodeUrl());
  }

  /**
   * Creates the backend using the configuration. Developers implementing custom backends will override this method.
   */
  protected static MNBackend getBackend(DataRepoBackendConfiguration configuration, Environment environment,
                                        DataRepoFsModule dataRepoModule) {
    return new DataRepoBackend(dataRepoModule.dataRepository(environment.getObjectMapper()),
                               dataRepoModule.doiRegistrationService(environment.getObjectMapper()),
                               configuration);
  }

  /**
   * Creates the backend using the configuration. Developers implementing custom backends will override this method.
   */
  protected static DataRepoFsModule getDataRepoFsModule(DataRepoBackendConfiguration configuration,
                                                        Environment environment) {
    DataRepoConfiguration dataRepoConfiguration = configuration.getDataRepoConfiguration();
    return new DataRepoFsModule(dataRepoConfiguration, environment.metrics(),
                                environment.healthChecks());

  }
}
