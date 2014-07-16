package org.gbif.d1.mn.rest;

import org.gbif.d1.mn.rest.provider.Authenticate;

import java.lang.reflect.Type;

import javax.ws.rs.ext.Provider;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;

/**
 * A utility to create Sessions.
 * Because authorization and authentication is well covered by unit tests we can test resources in isolation and without
 * Servlet container dependencies.
 */
@Provider
public class MockSessionProvider implements InjectableProvider<Authenticate, Type>, Injectable<Session> {

  public MockSessionProvider() {
  }

  @Override
  public Injectable<Session> getInjectable(ComponentContext ic, Authenticate a, Type c) {
    if (c.equals(Session.class)) {
      return this;
    }
    return null;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }

  @Override
  public Session getValue() {
    return Session.builder()
      .withSubject(
        Subject.builder().withValue("CN=Tim Robertson").build()
      ).build();
  }
}