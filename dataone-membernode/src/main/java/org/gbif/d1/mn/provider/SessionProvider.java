package org.gbif.d1.mn.provider;

import org.gbif.d1.mn.auth.CertificateUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

import org.dataone.ns.service.types.v1.Session;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A provider of {@link Session} from the HTTP request.
 * The {@link Authenticate} annotation should be used on method parameters to activate this provider.
 */
@Singleton
public class SessionProvider extends AbstractBinder {

  private final CertificateUtils certificateUtils;

  public SessionProvider(CertificateUtils certificateUtils) {this.certificateUtils = certificateUtils;}

  @Override
  protected void configure() {
    bind(SessionValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
    bind(SessionInjectionResolver.class).to(
      new TypeLiteral<InjectionResolver<Authenticate>>() {
      }
    ).in(Singleton.class);
    bind(certificateUtils).to(CertificateUtils.class);
  }


  public static class SessionInjectionResolver extends ParamInjectionResolver<Authenticate> {
    public SessionInjectionResolver() {
      super(SessionValueFactoryProvider.class);
    }
  }

  @Singleton
  public static class SessionValueFactoryProvider extends AbstractValueFactoryProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SessionValueFactoryProvider.class);
    private final CertificateUtils certificateUtils;

    @Inject
    public SessionValueFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
                                       final ServiceLocator injector,
                                       final CertificateUtils certificateUtils) {
      super(extractorProvider, injector, Parameter.Source.UNKNOWN);
      this.certificateUtils = certificateUtils;
    }

    @Override
    protected Factory<?> createValueFactory(final Parameter parameter) {
      final Class<?> classType = parameter.getRawType();
      final Authenticate authenticate = parameter.getAnnotation(Authenticate.class);
      // verify the annotation and class are of the expected type
      if (authenticate == null || classType == null ||
          !classType.equals(Session.class)) {
        return null;  // expected behavior
      }

      // otherwise build the session from the request
      return new AbstractContainerRequestValueFactory<Object>() {
        @Context
        private ResourceContext context;

        @Override
        public Session provide() {
          HttpServletRequest request = context.getResource(HttpServletRequest.class);
          if (request != null) {
            Session session = certificateUtils.newSession(request, authenticate.optional()); // throws Exception if auth fails
            LOG.debug("Successfully authenticated user {}", session.getSubject().getValue());
            return session;
          } else {
            throw new IllegalStateException("Session provider cannot provide sessions when no request is present");
          }
        }
      };
    }
  }
}
