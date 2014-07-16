package org.dataone.ns.service.types.v1;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.io.Resources;

/**
 * Utility for building of typed objects from files.
 */
public class Builders {

  private static final JAXBContext CONTEXT;
  static {
    try {
      CONTEXT =
        JAXBContext.newInstance(Node.class, Session.class, SubjectInfo.class, SystemMetadata.class, NodeList.class);
    } catch (JAXBException e) {
      throw new RuntimeException(
        "Unable to create the JAXBContex [something is wrong with the generated classes or JAXB libraries are missing]");
    }
  }

  public static Node newNode(String filename) {
    return newObject(filename, Node.class);
  }

  public static NodeList newNodeList(String filename) {
    return newObject(filename, NodeList.class);
  }

  public static Session newSession(String filename) {
    return newObject(filename, Session.class);
  }

  public static SubjectInfo newSubjectInfo(String filename) {
    return newObject(filename, SubjectInfo.class);
  }

  public static SystemMetadata newSystemMetadata(String filename) {
    return newObject(filename, SystemMetadata.class);
  }

  /**
   * Create the object from the given file which must be on the path.
   * 
   * @throws IllegalArgumentException if the file does not exist on the path or cannot be read as the stated type
   */
  @SuppressWarnings("unchecked")
  private static <T> T newObject(String filename, Class<T> type) {
    try {
      return (T) CONTEXT.createUnmarshaller().unmarshal(Resources.getResource(filename));
    } catch (Throwable e) {
      throw new IllegalArgumentException("Unable to convert file[" + filename + "] into type[" + type.getName() + "]",
        e);
    }
  }
}
