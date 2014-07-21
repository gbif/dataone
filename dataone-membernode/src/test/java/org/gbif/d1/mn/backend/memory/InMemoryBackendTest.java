package org.gbif.d1.mn.backend.memory;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InMemoryBackendTest {

  private static final DatatypeFactory DATATYPE_FACTORY = newDatatypeFactory();

  private static DatatypeFactory newDatatypeFactory() {
    try {
      return DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw Throwables.propagate(e);
    }
  }

  @Test
  public void testFilter() {
    LinkedHashMap<Identifier, PersistedObject> data = Maps.newLinkedHashMap();

    Date time1 = new Date();
    sleep(1); // Dates are only to 1 msecs precision
    appendData(data, "batch 1", "urn:node:1", "XML", 5);
    appendData(data, "batch 2", "urn:node:1", "CSV", 5);
    appendData(data, "batch 3", "urn:node:2", "XML", 5);
    appendData(data, "batch 4", "urn:node:2", "CSV", 5);
    sleep(1);
    Date time2 = new Date();
    sleep(1);
    appendData(data, "batch 5", "urn:node:1", "XML", 5);
    appendData(data, "batch 6", "urn:node:1", "CSV", 5);
    appendData(data, "batch 7", "urn:node:2", "XML", 5);
    appendData(data, "batch 8", "urn:node:2", "CSV", 5);
    sleep(1);
    Date time3 = new Date();

    InMemoryBackend backend = new InMemoryBackend(data);
    NodeReference self = NodeReference.builder().withValue("urn:node:1").build();

    assertTrue(Objects.equal(NodeReference.builder().withValue("urn:node:1").build(), NodeReference.builder()
      .withValue("urn:node:1").build()));

    assertEquals(40, backend.filter(self, time1, null, null, null, null, null).size());
    assertEquals(40, backend.filter(self, null, time3, null, null, null, null).size());
    assertEquals(20, backend.filter(self, time2, null, null, null, null, null).size());
    assertEquals(20, backend.filter(self, time1, time2, null, null, null, null).size());
    assertEquals(20, backend.filter(self, time2, time3, null, null, null, null).size());
    assertEquals(20, backend.filter(self, time1, time3, "XML", null, null, null).size());
    assertEquals(10, backend.filter(self, time2, time3, "XML", true, null, null).size());
    assertEquals(5, backend.filter(self, time2, time3, "XML", false, null, null).size());
    assertEquals(2, backend.filter(self, time2, time3, "XML", true, 0, 2).size());
    assertEquals(4, backend.filter(self, time2, time3, "XML", true, 0, 4).size());
    assertEquals(4, backend.filter(self, time2, time3, "XML", true, 2, 4).size());
    assertEquals(3, backend.filter(self, time2, time3, "XML", true, 6, 3).size());
    assertEquals(4, backend.filter(self, time2, time3, "XML", true, 6, 4).size());
    assertEquals(4, backend.filter(self, time2, time3, "XML", true, 6, 5).size());
    assertEquals(1, backend.filter(self, time2, time3, "XML", true, 9, 5).size());
    assertEquals(0, backend.filter(self, time2, time3, "XML", true, 10, 5).size());
  }

  private void appendData(LinkedHashMap<Identifier, PersistedObject> data, String prefix, String authoritativeMN,
    String formatId, int count) {
    for (int i = 0; i < count; i++) {
      Identifier pid = Identifier.builder().withValue(prefix + i).build();

      SystemMetadata sysmeta = SystemMetadata.builder()
        .withIdentifier(pid)
        .withDateSysMetadataModified(DATATYPE_FACTORY.newXMLGregorianCalendar(new GregorianCalendar()))
        .withAuthoritativeMemberNode(NodeReference.builder().withValue(authoritativeMN).build())
        .withFormatId(formatId)
        .build();

      data.put(pid, new PersistedObject("Mock".getBytes(), sysmeta, new Date(), new Date()));
    }
  }

  private void sleep(int msecs) {
    try {
      Thread.sleep(msecs);
    } catch (InterruptedException e) {
      Throwables.propagate(e);
    }
  }
}
