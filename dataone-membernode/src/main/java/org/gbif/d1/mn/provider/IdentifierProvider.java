package org.gbif.d1.mn.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.dataone.ns.service.types.v1.Identifier;

/**
 * ParamConverterProvider used to get parameter as DataONE Identifier.
 * Usage @PathParam("pid") Identifier pid
 */
@Provider
@Singleton
public class IdentifierProvider implements ParamConverterProvider {

  private ParamConverter PARAM_CONVERT_INSTANCE = new IdentifierParamConverter();

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
    if (rawType != Identifier.class) {
      return null;
    }
    return (ParamConverter<T>) PARAM_CONVERT_INSTANCE;
  }

  private static class IdentifierParamConverter implements ParamConverter<Identifier>{
    @Override
    public Identifier fromString(String value) throws IllegalArgumentException {
      return Identifier.builder().withValue(value).build();
    }

    @Override
    public String toString(Identifier identifier) throws IllegalArgumentException {
      return identifier.toString();
    }
  }
}