package org.gbif.d1.mn.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.dataone.ns.service.types.v1.DescribeResponse;

/**
 * Provider for the describe response HEAD request.
 */
@Provider
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class DescribeResponseHeaderProvider implements MessageBodyWriter<DescribeResponse> {

  @Override
  public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    return type == DescribeResponse.class;
  }

  @Override
  public long getSize(DescribeResponse describeResponse, Class<?> aClass, Type type, Annotation[] annotations,
                      MediaType mediaType) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return 0;
  }

  @Override
  public void writeTo(DescribeResponse describeResponse, Class<?> aClass, Type type, Annotation[] annotations,
                      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream outputStream
  ) throws IOException, WebApplicationException {
    writeHeader("DataONE-formatId", describeResponse.getObjectFormatID(), httpHeaders);
    Optional.ofNullable(describeResponse.getChecksum()).ifPresent( checksum -> {
      writeHeader("DataONE-Checksum", describeResponse.getChecksum().getAlgorithm() + ','
                                      + describeResponse.getChecksum().getValue(), httpHeaders);
    });
    writeHeader("DataONE-SerialVersion", describeResponse.getSerialVersion(), httpHeaders);
    writeHeader("Last-Modified", describeResponse.getLastModified(), httpHeaders);
    writeHeader("Content-Length", describeResponse.getContentLength(), httpHeaders);
  }

  private static <T> void writeHeader(String header, T value, MultivaluedMap<String, Object> httpHeaders) {
    Optional.ofNullable(value).ifPresent( headerValue ->  httpHeaders.add(header, headerValue));
  }
}
