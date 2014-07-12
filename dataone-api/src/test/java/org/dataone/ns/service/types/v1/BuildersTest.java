package org.dataone.ns.service.types.v1;

import org.junit.Test;

public class BuildersTest {

  @Test(expected = RuntimeException.class)
  public void testMissingFile() {
    Builders.newNode("notPresent.xml");
  }

  @Test
  public void testNewNode() {
    Builders.newNode("node.xml");
  }

  @Test
  public void testNewNodeList() {
    Builders.newNodeList("nodeList.xml");
  }

  @Test
  public void testNewSession() {
    Builders.newSession("session.xml");
  }

  @Test
  public void testNewSubjectInfo() {
    Builders.newSubjectInfo("subjectInfo.xml");
  }

  @Test
  public void testNewSystemMetadata() {
    Builders.newSystemMetadata("sysMeta.xml");
  }

  @Test(expected = RuntimeException.class)
  public void testWrongType() {
    // session is not a node
    Builders.newNode("session.xml");
  }
}
