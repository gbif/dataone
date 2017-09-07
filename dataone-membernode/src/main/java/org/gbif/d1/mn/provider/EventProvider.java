package org.gbif.d1.mn.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.dataone.ns.service.types.v1.Event;

/**
 * ParamConverterProvider used to get parameter as DataONE Event.
 * Usage @PathParam("event") Event event.
 */
@Provider
@Singleton
public class EventProvider implements ParamConverterProvider {

  private static final ParamConverter<Event> PARAM_CONVERT_INSTANCE = new EventParamConverter();

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
    if (rawType != Event.class) {
      return null;
    }
    return (ParamConverter<T>) PARAM_CONVERT_INSTANCE;
  }

  private static class EventParamConverter implements ParamConverter<Event>{
    @Override
    public Event fromString(String value) throws IllegalArgumentException {
      try {
        return Optional.ofNullable(value)
                .map(val -> Event.fromValue(val.toLowerCase()))
                .orElse(null);
      } catch (Exception ex) {
        throw new WebApplicationException("Wrong Event parameter", Response.Status.BAD_REQUEST);
      }
    }

    @Override
    public String toString(Event event) throws IllegalArgumentException {
      return event.value();
    }
  }
}
