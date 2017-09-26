package org.gbif.d1.mn.client;

import java.io.InputStream;
import java.util.Date;
import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.dataone.ns.service.apis.v1.mn.MNRead;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.SystemMetadata;

public class MNReadClient implements MNRead {

  private final WebTarget node;

  public MNReadClient(Client client, String nodeUrl) {
    node = client.target(nodeUrl + "mn/v1/");
  }

  @Override
  public InputStream get(Identifier pid) {
    return node.path("object/" + pid.getValue()).request(MediaType.APPLICATION_OCTET_STREAM).get(InputStream.class);
  }

  @Override
  public SystemMetadata getSystemMetadata(Identifier pid) {
    return node.path("meta/" + pid.getValue()).request(MediaType.APPLICATION_XML).get(SystemMetadata.class);
  }

  @Override
  public DescribeResponse describe(Identifier pid) {
    return node.path("describe/" + pid.getValue()).request(MediaType.APPLICATION_XML).get(DescribeResponse.class);
  }

  @Override
  public ObjectList listObjects(Date fromDate, @Nullable Date toDate, @Nullable String formatId,
                                @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count) {
    return node.path("object")
            .queryParam("fromDate", fromDate)
            .queryParam("toDate", toDate)
            .queryParam("toDate", toDate)
            .queryParam("formatId", formatId)
            .queryParam("replicaStatus", replicaStatus)
            .queryParam("start", start)
            .queryParam("count", count)
            .request(MediaType.APPLICATION_OCTET_STREAM)
            .get(ObjectList.class);
  }

  @Override
  public boolean synchronizationFailed(ExceptionDetail detail) {
    return node.path("error").request(MediaType.APPLICATION_XML).post(Entity.entity(detail, MediaType.APPLICATION_XML),Boolean.class);
  }

  @Override
  public InputStream getReplica(Identifier pid) {
    return node.path("replica/" + pid.getValue()).request(MediaType.APPLICATION_OCTET_STREAM).get(InputStream.class);
  }
}
