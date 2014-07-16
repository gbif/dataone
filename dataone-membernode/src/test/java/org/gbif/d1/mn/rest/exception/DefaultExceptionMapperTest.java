package org.gbif.d1.mn.rest.exception;

import org.gbif.d1.mn.rest.exception.DataONE.Method;

import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedMetadataType;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DefaultExceptionMapperTest {

  @Test
  public void testDetailCode() {
    DefaultExceptionMapper mapper = new DefaultExceptionMapper("urn:node:Tim");

    DataONE annotation = Mockito.mock(DataONE.class);
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
  public void testToResponse() {
    // TODO: verify a couple examples actually work ja [including some unusual exceptions]
  }
}
