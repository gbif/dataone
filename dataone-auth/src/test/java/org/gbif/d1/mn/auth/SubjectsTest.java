package org.gbif.d1.mn.auth;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.dataone.ns.service.types.v1.Session;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.d1.mn.auth.Subjects.AUTHENTICATED_SUBJECT;
import static org.gbif.d1.mn.auth.Subjects.PUBLIC_SUBJECT;
import static org.gbif.d1.mn.auth.Subjects.VERIFIED_SUBJECT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubjectsTest {

  private static final Logger LOG = LoggerFactory.getLogger(SubjectsTest.class);

  /**
   * Reads the session and makes sure the expected results are found.
   */
  private void runSubjectExpansionTest(Session session, Set<String> expected) {
    LOG.debug("Session: {}", session);
    Set<String> subjects = Subjects.allSubjects(session);
    LOG.debug("Subjects found: {}", subjects);
    Set<String> difference = Sets.symmetricDifference(expected, subjects);
    LOG.debug("Difference [expected to be empty]: {}", difference);
    assertTrue(difference.isEmpty());
  }

  @Test
  public void testAllSubjects1() throws Exception {
    // the simplest of all possibilities
    runSubjectExpansionTest(
      TestFiles.newSession("org/gbif/d1/mn/auth/session-1.xml"),
      ImmutableSet.of(PUBLIC_SUBJECT, AUTHENTICATED_SUBJECT, "CN=Tim Robertson"));
  }

  @Test
  public void testAllSubjects2() throws Exception {
    runSubjectExpansionTest(
      TestFiles.newSession("org/gbif/d1/mn/auth/session-2.xml"),
      ImmutableSet.of(PUBLIC_SUBJECT, AUTHENTICATED_SUBJECT, "CN=Tim Robertson", "O=GBIF"));
  }

  @Test
  public void testAllSubjects3() throws Exception {
    runSubjectExpansionTest(
      TestFiles.newSession("org/gbif/d1/mn/auth/session-3.xml"),
      ImmutableSet.of(PUBLIC_SUBJECT, AUTHENTICATED_SUBJECT, VERIFIED_SUBJECT, "CN=Tim Robertson", "O=GBIF"));
  }

  @Test
  public void testAllSubjects4() throws Exception {
    runSubjectExpansionTest(
      TestFiles.newSession("org/gbif/d1/mn/auth/session-4.xml"),
      ImmutableSet.of(PUBLIC_SUBJECT, AUTHENTICATED_SUBJECT, "CN=Tim Robertson"));
  }

  @Test
  public void testAllSubjects5() throws Exception {
    // this example highlights equivalent identities and the case where groups have users where only the equivalent
    // identity is added to the group membership
    runSubjectExpansionTest(
      TestFiles.newSession("org/gbif/d1/mn/auth/session-5.xml"),
      ImmutableSet.of(PUBLIC_SUBJECT, AUTHENTICATED_SUBJECT, VERIFIED_SUBJECT,
        "CN=Tim Robertson",
        "O=GBIF",
        "O=Acme",
        "CN=Timo",
        "CN=Timothy",
        "O=DataONE",
        "CN=Timo"));
  }

  @Test
  public void testIndexGroupMembership() throws JAXBException, IOException {
    Map<String, Set<String>> index =
      Subjects.indexGroupMembership(TestFiles.newSubjectInfo("org/gbif/d1/mn/auth/subjectInfo-2.xml"));
    assertEquals(4, index.keySet().size());
    assertTrue(index.containsKey("CN=Tim Robertson"));
    assertTrue(index.containsKey("CN=Federico Mendez"));
    assertTrue(index.containsKey("CN=Timo"));
    assertTrue(index.containsKey("CN=Dave Vieglas"));
    assertEquals(2, index.get("CN=Tim Robertson").size());
    assertEquals(1, index.get("CN=Federico Mendez").size());
    assertEquals(1, index.get("CN=Timo").size());
    assertEquals(1, index.get("CN=Dave Vieglas").size());
  }
}
