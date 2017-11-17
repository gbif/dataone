package org.gbif.d1.mn.exception;

import java.io.File;
import javax.xml.bind.JAXB;

import org.dataone.ns.service.types.v1.SystemMetadata;

public class DateTest {

  public static void main (String[] args) throws Exception {
    //2012-02-20T20:39:19.70598
    SystemMetadata systemMetadata = JAXB.unmarshal(new File("/Users/fmendez/dev/git/gbif/dataone/dataone-membernode/conf/examples/resourceMap/metadata.xml"),
                   SystemMetadata.class);
    System.out.println(systemMetadata.getDateSysMetadataModified().toGregorianCalendar());
    System.out.println(systemMetadata.getDateSysMetadataModified().toGregorianCalendar().getTime());
  }
}
