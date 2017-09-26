package org.gbif.d1.mn;

import org.gbif.d1.mn.auth.AuthorizationManager;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;
import org.dataone.ns.service.types.v1.Node;

/**
 * Application configuration with sensible defaults if applicable.
 */
public class MNConfiguration extends Configuration {

  public enum TIER {
    TIER1, TIER2, TIER3, TIER4
  }

  private String externalUrl;

  private String coordinatingNodeUrl;

  private Node node;

  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  private List<String> trustedOIDs = Lists.newArrayList(AuthorizationManager.DEFAULT_OID_SUBJECT_INFO);

  public Tier getTier(){
    return Tier.TIER4;
  }


  @JsonProperty
  public String getCoordinatingNodeUrl() {
    return coordinatingNodeUrl;
  }

  public void setCoordinatingNodeUrl(String coordinatingNodeUrl) {
    this.coordinatingNodeUrl = coordinatingNodeUrl;
  }

  @JsonProperty
  public JerseyClientConfiguration getJerseyClient() {
    return jerseyClient;
  }

  @JsonProperty
  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public List<String> getTrustedOIDs() {
    return trustedOIDs;
  }

  public void setTrustedOIDs(List<String> trustedOIDs) {
    this.trustedOIDs = trustedOIDs;
  }
}
