package org.gbif.d1.mn.rest;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import org.mockito.Mockito;

/**
 * TODO: currently exploring why injecting an HttpServletRequest stops tests working.
 * Ignore this class.
 */
public class MockRequestProvider extends AbstractHttpContextInjectable<HttpServletRequest> implements
  InjectableProvider<Context, Type> {

  @Override
  public Injectable<HttpServletRequest> getInjectable(ComponentContext ic, Context a, Type c) {
    if (c.equals(HttpServletRequest.class)) {
      return this;
    }
    return null;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }

  @Override
  public HttpServletRequest getValue(HttpContext c) {
    return Mockito.mock(HttpServletRequest.class);
  }

}
