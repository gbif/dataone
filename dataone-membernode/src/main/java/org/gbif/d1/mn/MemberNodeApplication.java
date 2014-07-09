package org.gbif.d1.mn;

import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.memory.InMemoryBackend;
import org.gbif.d1.mn.health.BackendHealthCheck;
import org.gbif.d1.mn.rest.MemberNodeResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.Service;
import org.dataone.ns.service.types.v1.Services;

/**
 * The main entry point for running the member node.
 */
// public class MemberNodeApplication extends Application<MemberNodeConfiguration> {
public class MemberNodeApplication<T extends MemberNodeConfiguration> extends Application<T> {

  private static final String APPLICATION_NAME = "DataONE Member Node";

  public static void main(String[] args) throws Exception {
    new MemberNodeApplication<MemberNodeConfiguration>().run(args);
  }

  protected MNBackend getBackend(T configuration) {
    System.out.println("Using parent backend");
    return new InMemoryBackend();
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

//
// ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
// .configure()
// .addValidatedValueHandler(new OptionalValidatedValueUnwrapper())
// .buildValidatorFactory();
// ConfigurationFactory<BackendConfiguration> configFactory =
// new ConfigurationFactory<>(BackendConfiguration.class, validatorFactory.getValidator(),
// Jackson.newObjectMapper(), "");
// BackendConfiguration backendConfig =
// configFactory.build(new FileConfigurationSourceProvider(), "conf/backend.yml");

    // TODO: how to make this configurable during build time?
    // some kind of factory and mvn classifier?
    MNBackend backend = getBackend(configuration);

    // RESTful resources
    environment.jersey().register(new MemberNodeResource(backend, thisNode(configuration)));

    // DW does bad things with exceptions?
    // http://thoughtspark.org/2013/02/25/dropwizard-and-jersey-exceptionmappers/
    // ResourceConfig jrConfig = environment.jersey().getResourceConfig();
    // jrConfig.getSingletons();
    // etc

    // also look at
    // public abstract class AbstractRootElementProvider extends AbstractJAXBProvider<Object> {
    // line 113

    // health checks
    environment.healthChecks().register("backend", new BackendHealthCheck(backend));
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

}