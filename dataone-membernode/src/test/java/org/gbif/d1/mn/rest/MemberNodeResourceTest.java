package org.gbif.d1.mn.rest;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.sun.jersey.api.client.WebResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.dataone.ns.service.apis.v1.MNAuthorization;
import org.dataone.ns.service.apis.v1.MNRead;
import org.dataone.ns.service.apis.v1.MNReplication;
import org.dataone.ns.service.apis.v1.MNStorage;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.hamcrest.MatcherAssert;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for URL routing and that the delegate services are called as expected.
 */
public class MemberNodeResourceTest {

  private static final String BASE_URL = "/mn/v1";

  // mock delegates for the resource
  private static final MNRead read = mock(MNRead.class);
  private static final MNAuthorization authorization = mock(MNAuthorization.class);
  private static final MNStorage storage = mock(MNStorage.class);
  private static final MNReplication replication = mock(MNReplication.class);

  private static final String DATE_FORMAT = "HH:mm:ss Z 'on' EEE, MMM d, yyyy";
  private static final DateTimeFormatter DTF = DateTimeFormat.forPattern(DATE_FORMAT); // threadsafe

  /**
   * We mock sessions because we can't access the HttpServletRequest using the Jersey InMemory container, and we don't
   * need to test authentication as that is unit tested in isolation.
   */
  @ClassRule
  public static ResourceTestRule resources = ResourceTestRule.builder()
    .addResource(new MemberNodeResource(read, authorization, storage, replication))
    .addProvider(new MockSessionProvider())
    .build();

  @Before
  public void resetInjected() {
    // Mockito cautions against use of this design except for this scenario
    reset(read, authorization, storage, replication);
  }

  @Test
  public void testPing() {
    when(read.ping()).thenReturn(DTF.print(new DateTime()));
    // if a ping takes longer than 5 secs we're hosed
    String pong = clientResource().path("monitor/ping").get(String.class);
    Date result = DTF.parseDateTime(pong).toDate();
    MatcherAssert.assertThat(result, DateMatchers.within(5, TimeUnit.SECONDS, new Date()));
    verify(read, times(1)).ping();
  }

  @Test(expected = ServiceFailure.class)
  public void testPingFail() {
    when(read.ping()).thenThrow(new ServiceFailure("Should propagate"));
    clientResource().path("monitor/ping").get(String.class);
  }

  /**
   * @return A client resource that can be used as the base for all test
   */
  private WebResource clientResource() {
    return resources.client().resource(BASE_URL);
  }
}