package org.gbif.d1.mn.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps all Throwable exceptions into a ServiceFailure with a default detail code.
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);
  private static final DataOneExceptionMapper DATA_ONE_MAPPER = new DataOneExceptionMapper();
  private static final String DEFAULT_DETAIL_CODE = "000"; // does not comply with the spec but what else can we do?

  @Override
  public Response toResponse(Throwable exception) {

    // If the exception is a wrapper around a DataONE exception, extract it and use default handling
    // Provider exceptions for example are wrapped up ContainerException for example
    if (exception.getCause() != null && exception.getCause() instanceof DataONEException) {
      return DATA_ONE_MAPPER.toResponse((DataONEException) exception.getCause());
    } else {
      // default behavior is a service failure
      LOG.warn("Unhandled exception[{}] handled as ServiceFailure", exception.getClass().getSimpleName());
      return DATA_ONE_MAPPER.toResponse(new ServiceFailure("Unexpected error occurred: " + exception.getMessage(),
        DEFAULT_DETAIL_CODE));
    }
  }
}
