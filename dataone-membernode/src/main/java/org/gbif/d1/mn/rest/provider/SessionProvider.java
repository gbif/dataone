package org.gbif.d1.mn.rest.provider;

import org.gbif.d1.mn.auth.CertificateUtils;

import java.lang.reflect.Type;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import org.dataone.ns.service.types.v1.Session;

/**
 * A provider of sessions which are built from the certificate presented with client requests.
 */
@Provider
public final class SessionProvider implements InjectableProvider<Authenticate, Type> {

  /**
   * This exists only because we need to surface the detail code in the exception.
   */
  private class SessionInjectable implements Injectable<Session> {

    private final String detailCode;

    public SessionInjectable(String detailCode) {
      this.detailCode = detailCode;
    }

    @Override
    public Session getValue() {
      try {
        return certificateUtils.newSession(request, detailCode);
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
  }

  @Context
  private HttpServletRequest request;

  private final CertificateUtils certificateUtils;

  // not for others to instantiate
  private SessionProvider(List<String> extensionOIDs) {
    certificateUtils = CertificateUtils.newInstance(extensionOIDs);
  }

  public static SessionProvider newWithDeclaredOIDs(List<String> extensionOIDs) {
    return new SessionProvider(extensionOIDs);
  }

  public static SessionProvider newWithDefaults() {
    return new SessionProvider(ImmutableList.<String>of());
  }

  @Override
  public Injectable<Session> getInjectable(ComponentContext ic, Authenticate a, Type c) {
    if (c.equals(Session.class)) {
      return new SessionInjectable(a.value());
    }
    return null;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }
}