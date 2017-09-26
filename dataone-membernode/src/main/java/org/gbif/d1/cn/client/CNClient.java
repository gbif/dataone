package org.gbif.d1.cn.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.http.client.methods.RequestBuilder;
import org.dataone.ns.service.apis.v1.cn.CoordinatingNode;
import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeList;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CNClient implements CoordinatingNode {

  private static final Logger LOG = LoggerFactory.getLogger(CNClient.class);

  private final WebTarget node;


  //This string literal is used only to access the cache
  private static final String NODES_CACHE = "nodes";

  //This cache avoids contacting the coordinating node each time the list of nodes is required
  private final LoadingCache<String,NodeList> nodesCache;


  private Response execute(Invocation.Builder request) {
    Response response = request.get(Response.class);
    Response.Status.Family responseFamily = response.getStatusInfo().getFamily();
    if (responseFamily == Response.Status.Family.CLIENT_ERROR || responseFamily == Response.Status.Family.SERVER_ERROR) {
      ExceptionDetail exceptionDetail = response.readEntity(ExceptionDetail.class);
      throw new ServiceFailure(exceptionDetail.getDescription(), exceptionDetail.getDetailCode(), exceptionDetail.getNodeId());
    }
    return response;
  }

  /**
   * Builds a CN client instance.
   * @param client jersey managed client
   * @param nodeUrl CN
   */
  public CNClient(Client client, String nodeUrl) {
    node = client.target(nodeUrl + "cn/v1/");
    nodesCache = CacheBuilder.newBuilder()
                  .expireAfterAccess(1, TimeUnit.HOURS) //expire cache every hour
                  .build(new CacheLoader<String, NodeList>() {
                    @Override
                    public NodeList load(String key) throws Exception {
                      return node.path("node/").request().accept(MediaType.APPLICATION_XML).get(NodeList.class);
                    }
                  });
  }

  @Override
  public NodeList listNodes() throws ServiceFailure {
    try {
      return nodesCache.get(NODES_CACHE);
    } catch (ExecutionException ee) {
      LOG.error("Error loading list of nodes", ee);
      throw new ServiceFailure("Error loading list of Coordinating Nodes");
    }
  }


  @Override
  public SystemMetadata getSystemMetadata(Identifier identifier) throws InvalidToken, NotImplemented, NotAuthorized,
    NotFound, ServiceFailure {
    return node.path("meta/" + identifier.getValue()).request().accept(MediaType.APPLICATION_XML)
            .get(SystemMetadata.class);
  }

  @Override
  public boolean isNodeAuthorized(Subject targetNodeSubject, String pid) {
    return Response.Status.Family.SUCCESSFUL == execute(node.path("replicaAuthorizations/" + pid)
      .queryParam("targetNodeSubject", targetNodeSubject.getValue())
      .request(MediaType.APPLICATION_XML)).getStatusInfo().getFamily();
  }

  @Override
  public Node getNodeCapabilities(String nodeId) {
    return execute(node.path("/node").path(nodeId).request(MediaType.APPLICATION_XML)).readEntity(Node.class);
  }


}
