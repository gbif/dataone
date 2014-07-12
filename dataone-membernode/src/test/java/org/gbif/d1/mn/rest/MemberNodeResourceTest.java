package org.gbif.d1.mn.rest;

import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.d1.mn.backend.model.MNLogEntry;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.dataone.ns.service.types.v1.Builders;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A full member node testing stack.
 * <p>
 * Uses mocked backend to assert correct behaviour and mock callback to ensure the correct CN callbacks are issued.
 */
public class MemberNodeResourceTest {

  private static final String BASE_URL = "/mn/v1";

  private static final MNBackend backend = mock(MNBackend.class);

  /**
   * We mock sessions because we can't access the HttpServletRequest using the Jersey InMemory container, and we don't
   * need to test authentication.
   */
  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
    .addResource(new MemberNodeResource(backend, Builders.newNode("node.xml")))
    .addProvider(new MockSessionProvider())
    .build();

  /**
   * To use this: clientResource().path("ping").get().
   * 
   * @return A client resource that can be used as the base for all test.
   */
  private WebResource clientResource() {
    return resources.client().resource(BASE_URL);
  }

  @Before
  public void setup() {
    when(backend.getLogs()).thenReturn(Lists.<MNLogEntry>newArrayList());
  }

  @Test
  public void testPing() {
    // if a ping takes longer than 5 secs I'd say we're hosed
    String pong = clientResource().path("monitor/ping").get(String.class);
    Date result = MemberNodeResource.DTF.parseDateTime(pong).toDate();
    MatcherAssert.assertThat(result, DateMatchers.within(5, TimeUnit.SECONDS, new Date()));
  }
}