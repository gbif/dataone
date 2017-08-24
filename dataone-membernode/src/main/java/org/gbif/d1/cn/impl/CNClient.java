package org.gbif.d1.cn.impl;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.NodeList;

public class CNClient implements CoordinatingNode {

  private final Client client;
  private final String coordinatingNodeUrl;

  public CNClient(Client client, String coordinatingNodeUrl) {
    this.client = client;
    this.coordinatingNodeUrl = coordinatingNodeUrl + "cn/v1/node/";
  }

  @Override
  public NodeList listNodes() throws ServiceFailure {
    return client.target(coordinatingNodeUrl).request().accept(MediaType.APPLICATION_XML).get(NodeList.class);
  }
}
