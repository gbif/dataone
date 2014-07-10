package org.gbif.d1.mn.rest.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import ch.qos.logback.core.status.Status;

/**
 * TODO: Make this support the DataONE API requirements of course.
 */
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable exception) {
    return Response
      .status(Status.ERROR)
      .entity(exception)
      .build();
  }
}
