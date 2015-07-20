package org.gbif.d1.mn.resource;

import org.gbif.d1.mn.auth.AuthorizationManager;

import javax.servlet.http.HttpServletRequest;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Tests for URL routing and that the delegate services are called as expected.
 */
public class ArchiveResourceTest {

  private static final HttpServletRequest request = mock(HttpServletRequest.class);
  private static final AuthorizationManager auth = mock(AuthorizationManager.class);

  @ClassRule
  public static ResourceTestRule resources = ResourceTestRule.builder()
    .addProvider(new ContextInjectableProvider<>(HttpServletRequest.class, request))
    .addResource(new ArchiveResource(auth))
    .build();

  @Test
  public void testArchive() {
    resources.client().resource("/mn/v1/archive/123").put();
  }
}