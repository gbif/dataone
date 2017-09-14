package org.gbif.d1.mn.exception;

import org.gbif.d1.mn.exception.DataONE.Method;

import javax.ws.rs.core.Response;

import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedMetadataType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultExceptionMapperTest {

  private static final String NODE_ID = "urn:node:tim";

  @Test
  public void testDetailCode() {
    DefaultExceptionMapper mapper = new DefaultExceptionMapper(NODE_ID);

    DataONE annotation = mock(DataONE.class);
    when(annotation.value()).thenReturn(Method.CREATE);

    String fail = DetailCodes.codeFor(Method.CREATE, ServiceFailure.class);
    String auth = DetailCodes.codeFor(Method.CREATE, NotAuthorized.class);

    assertEquals(fail, mapper.detailCode(annotation, new ServiceFailure("Ignored")));
    assertEquals(fail + ".123", mapper.detailCode(annotation, new ServiceFailure("Ignored", "123")));
    assertEquals(fail + ".123", mapper.detailCode(annotation, new ServiceFailure("Ignored", ".123")));

    assertEquals(auth, mapper.detailCode(annotation, new NotAuthorized("Ignored")));
    assertEquals(auth + ".123", mapper.detailCode(annotation, new NotAuthorized("Ignored", "123")));
    assertEquals(auth + ".123", mapper.detailCode(annotation, new NotAuthorized("Ignored", ".123")));

    // Important: this test relies on CREATE not being able to throw an UnsupportedMetadataType.
    // In this case we designed it to log warning and return a default as it implies our code is throwing exceptions it
    // should not be
    assertEquals(DefaultExceptionMapper.DEFAULT_DETAIL_CODE,
      mapper.detailCode(annotation, new UnsupportedMetadataType("Ignored")));

  }

  @Test
  public void testNotFound() {
    testExceptionScenario(new DefaultExceptionMapper(NODE_ID), Method.UPDATE, new NotFound("Test", "1"));
  }

  @Test
  public void testNotImplemented() {
    testExceptionScenario(new DefaultExceptionMapper(NODE_ID), Method.REPLICATE, new NotImplemented("Test"));
  }

  @Test
  public void testServiceFailure() {
    testExceptionScenario(new DefaultExceptionMapper(NODE_ID), Method.CREATE, new ServiceFailure("Test"));
  }

  /**
   * Tests that the exception mapper builds correctly for the given scenario.
   * <p>
   * Makes use of the {@link DetailCodes} and {@link HttpCodes} which are assumed to be correct.
   */
  private void testExceptionScenario(DefaultExceptionMapper mapper, Method method, DataONEException cause) {
    DataONE annotation = mock(DataONE.class);
    when(annotation.value()).thenReturn(method);

    Response response = mapper.toResponse(cause, annotation);
    assertNotNull("Response must exist", response);

    assertNotNull("Entity should be populated", response.getEntity());
    assertEquals(ExceptionDetail.class, response.getEntity().getClass());
    ExceptionDetail detail = (ExceptionDetail) response.getEntity();
    assertEquals(NODE_ID, detail.getNodeId());
    assertEquals(cause.getMessage(), detail.getDescription());
    assertEquals(cause.getPid(), detail.getPid());
    assertEquals((int) HttpCodes.codeFor(cause.getClass()), detail.getErrorCode());
    assertEquals(DetailCodes.codeFor(method, cause.getClass()), detail.getDetailCode());

    assertNotNull("HTTP code should be populated", HttpCodes.codeFor(cause.getClass()));
    assertEquals((int) HttpCodes.codeFor(cause.getClass()), response.getStatus());
  }
}
