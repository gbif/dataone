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

import org.dataone.ns.service.types.v1.Permission;

/**
 * ParamConverterProvider used to get parameter as DataONE Event.
 * Usage @PathParam("permission") Permission permission.
 */
@Provider
@Singleton
public class PermissionProvider implements ParamConverterProvider {

  private static final ParamConverter<Permission> PARAM_CONVERT_INSTANCE = new PermissionParamConverter();

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
    if (rawType != Permission.class) {
      return null;
    }
    return (ParamConverter<T>) PARAM_CONVERT_INSTANCE;
  }

  private static class PermissionParamConverter implements ParamConverter<Permission>{
    @Override
    public Permission fromString(String value) throws IllegalArgumentException {
      try {
        return Optional.ofNullable(value)
          .map(val -> Permission.fromValue(val))
          .orElse(null);
      } catch (Exception ex) {
        throw new WebApplicationException("Wrong Event parameter", Response.Status.BAD_REQUEST);
      }
    }

    @Override
    public String toString(Permission permission) throws IllegalArgumentException {
      return permission.value();
    }
  }
}
