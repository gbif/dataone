package org.gbif.d1.mn.exception;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.Invokable;
import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.internal.ModelProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes exceptions according to the DataONE specification.
 * <p>
 * The DataONE specification is strict in that there are specific codes that must be set in the response depending on
 * the method called (sigh).  To stop having to keep a trace within code, this software architecture simply annotates
 * each method and then if an exception is thrown, this class deals with the handling of reporting the case to DataONE
 * by inspecting the method annotation.  Thus, the mess is contained within this class, and individual cases don't need
 * to concern themselves with codes and serialization and can simply throw standard exceptions.
 * <p>
 * We inspect annotations on the original target method to populate the detailCode and PID should they not be
 * present in DataONE exceptions, or prefix existing ones with the detail code using dot notation should they already
 * exist. This is in accordance with the DataONE specification.
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);
  private final String nodeId;

  @VisibleForTesting
  static final String DEFAULT_DETAIL_CODE = "000"; // does not comply with the specification but what else can we do?

  @VisibleForTesting
  static final int DEFAULT_HTTP_CODE = 500; // for safety only

  // required to access the annotation to populate the detail code
  @Context
  private ExtendedUriInfo uriInfo;

  public DefaultExceptionMapper(String nodeId) {
    this.nodeId = nodeId;
  }

  @Override
  public Response toResponse(Throwable exception) {

    // check if it is a URL that we support
    if (uriInfo.getMatchedResourceMethod() != null) {

      // inspect the method annotation and lookup the detailCode for the provided "exception / method" pair
      DataONE dataONE = uriInfo.getMatchedResourceMethod().getInvocable()
        .getHandlingMethod().getAnnotation(DataONE.class);

      if (dataONE != null) {
        return toResponse(exception, dataONE);
      } else {
        // swallow gracefully
        LOG.warn("Expected a DataONE annotation on method linked to {}", uriInfo);
      }
    }

    // not a URL this application supports
    LOG.debug("Not found: {}", uriInfo.getPath());
    return Response.status(Status.NOT_FOUND).build();
  }

  @VisibleForTesting
  Response toResponse(Throwable exception, DataONE dataONE) {
    // all exceptions are coorced to DataONE exceptions for serialization
    DataONEException d1e = coerce(exception);
    String detailCode = detailCode(dataONE, d1e);

    // HTTP code is mapped 1:1 with the DataONEException
    int code = httpCode(d1e);
    return Response
      .status(code)
      .type(MediaType.APPLICATION_XML)
      .entity(new ExceptionDetail(
        d1e.getClass().getSimpleName(), code, detailCode, d1e.getMessage(), nodeId, d1e.getPid())
      )
      .build();
  }

  /**
   * Coerces the exception into a DataONEException defaulting as a ServiceFailure if required.
   */
  private DataONEException coerce(Throwable exception) {
    if (exception instanceof DataONEException) {
      return (DataONEException) exception;

    } else if (exception.getCause() != null && exception.getCause() instanceof DataONEException) {
      // could be a Jersey wrapped exception for example
      return (DataONEException) exception.getCause();

    } else if (exception instanceof NullPointerException) {
      // we use Preconditions to throw NPE on Invalid requests
      return new InvalidRequest(exception.getMessage(), exception);

    } else {
      // we default as a service failure
      return new ServiceFailure(exception.getMessage(), exception);
    }
  }

  private int httpCode(DataONEException d1e) {
    Integer code = HttpCodes.codeFor(d1e.getClass()); // can return null
    if (code == null) {
      LOG.warn("Missing HTTP code for exception[{}], using default[{}]", d1e.getClass().getSimpleName(),
        DEFAULT_HTTP_CODE);
      return DEFAULT_HTTP_CODE;
    } else {
      return code;
    }
  }

  /**
   * Determine the detail code for the given annotation and exception.
   * <p>
   * We make use of {@link DetailCodes} to determine the registered codes, and concatenate using dot notation if the
   * exception already has a code in accordance with the specification.
   */
  @VisibleForTesting
  String detailCode(DataONE dataONE, DataONEException d1e) {
    DataONE.Method method = dataONE == null ? null : dataONE.value();

    // lookup the registered code (prefix) for the method and annotation
    String prefix = null;
    if (method == null) {
      LOG.warn("Method [{}] missing DataONE annotation", uriInfo.getMatchedResourceMethod());
    } else {
      prefix = DetailCodes.codeFor(method, d1e.getClass()); // may return null
    }

    if (d1e.getDetailCode() == null) {
      // the exception did not have one, and we return the registered code or default
      if (prefix == null) {
        LOG.warn("No registered detailCode exists for method[{}] and exception[{}] - using default[{}]", method, d1e
          .getClass().getSimpleName(), DEFAULT_DETAIL_CODE);
        return DEFAULT_DETAIL_CODE;
      } else {
        return prefix;
      }

    } else if (prefix == null) {
      // the exception has a code already, and there is no registered code
      return d1e.getDetailCode();

    } else {
      // concatenate only if it is not already present and return
      if (d1e.getDetailCode().startsWith(prefix)) {
        return d1e.getDetailCode();
      } else {
        // careful not to introduce double dots
        return prefix + "." + d1e.getDetailCode().replaceFirst("\\.", "");
      }
    }
  }
}
