package org.gbif.d1.mn.auth;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeList;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SubjectInfo;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Utility for building of typed objects from files for tests.
 */
class TestFiles {

  private static final JAXBContext context;
  static {
    try {
      context =
        JAXBContext.newInstance(Node.class, Session.class, SubjectInfo.class, SystemMetadata.class, NodeList.class);
    } catch (JAXBException e) {
      throw Throwables.propagate(e); // we're hosed
    }
  }

  static Node newNode(String filename) throws JAXBException, IOException {
    return (Node) context.createUnmarshaller().unmarshal(Resources.getResource(filename));
  }

  static NodeList newNodeList(String filename) throws JAXBException, IOException {
    return (NodeList) context.createUnmarshaller().unmarshal(Resources.getResource(filename));
  }

  static Session newSession(String filename) throws JAXBException, IOException {
    return (Session) context.createUnmarshaller().unmarshal(Resources.getResource(filename));
  }

  static SubjectInfo newSubjectInfo(String filename) throws JAXBException, IOException {
    return (SubjectInfo) context.createUnmarshaller().unmarshal(Resources.getResource(filename));
  }

  static SystemMetadata newSystemMetadata(String filename) throws JAXBException, IOException {
    return (SystemMetadata) context.createUnmarshaller().unmarshal(Resources.getResource(filename));
  }
}
