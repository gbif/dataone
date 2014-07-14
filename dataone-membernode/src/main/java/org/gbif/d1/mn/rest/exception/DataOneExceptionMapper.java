package org.gbif.d1.mn.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.HttpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Make this support the DataONE API requirements of course.
 */
@Provider
public class DataOneExceptionMapper implements ExceptionMapper<DataONEException> {

  private static final Logger LOG = LoggerFactory.getLogger(DataOneExceptionMapper.class);

  @Override
  public Response toResponse(DataONEException exception) {
    LOG.error("Throwing exception[{}]", exception.getClass().getSimpleName(), exception);
    return Response
      .status(HttpCodes.forType(exception))
      .entity("" + exception.getMessage()) // TODO: turn into a Exception detail and marshal as XML
      .build();
  }
}
