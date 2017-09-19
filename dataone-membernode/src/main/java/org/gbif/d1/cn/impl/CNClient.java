package org.gbif.d1.cn.impl;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.NodeList;
import org.dataone.ns.service.types.v1.SystemMetadata;

public class CNClient implements CoordinatingNode {

  private final Client client;
  private final String coordinatingNodeUrl;

  public CNClient(Client client, String coordinatingNodeUrl) {
    this.client = client;
    this.coordinatingNodeUrl = coordinatingNodeUrl;
  }

  @Override
  public NodeList listNodes() throws ServiceFailure {
    return client.target(coordinatingNodeUrl + "cn/v1/node/").request().accept(MediaType.APPLICATION_XML).get(NodeList.class);
  }

  @Override
  public SystemMetadata getSystemMetadata(Identifier identifier) throws InvalidToken, NotImplemented, NotAuthorized,
    NotFound, ServiceFailure {
    return client.target(coordinatingNodeUrl + "cn/v1/meta/" + identifier.getValue()).request()
            .accept(MediaType.APPLICATION_XML).get(SystemMetadata.class);
  }
}
