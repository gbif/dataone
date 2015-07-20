package org.gbif.d1.mn.resource;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

/**
 * This exists only to allow in memory Jersey testing with field level {@link Context} annotations.
 * 
 * @see <a href="https://github.com/dropwizard/dropwizard/issues/651">github commentary</a>
 */
final class ContextInjectableProvider<T> extends SingletonTypeInjectableProvider<Context, T> {

  ContextInjectableProvider(Type type, T instance) {
    super(type, instance);
  }
}
