package org.gbif.d1.mn.backend.impl;

import org.gbif.d1.mn.MNConfiguration;
import org.gbif.d1.mn.Tier;
import org.gbif.datarepo.store.fs.conf.DataRepoConfiguration;
import org.gbif.discovery.conf.ServiceConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.logging.LoggingUtil;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Configuration settings of a backend supported on the GBIF data repo.
 */
public class DataRepoBackendConfiguration extends MNConfiguration {

  private static final LogbackAutoConfigLoggingFactory LOGGING_FACTORY = new LogbackAutoConfigLoggingFactory();

  /**
   * Configuration specific to interfacing with elastic search.
   */
  public static class ElasticSearch {


    public String host = "localhost";

    public int port = 9300;

    public String cluster = "content-cluster";

    public String idx = "logstash-datanone-*";

    @JsonProperty
    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    @JsonProperty
    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    @JsonProperty
    public String getCluster() {
      return cluster;
    }

    public void setCluster(String cluster) {
      this.cluster = cluster;
    }

    @JsonProperty
    public String getIdx() {
      return idx;
    }

    public void setIdx(String idx) {
      this.idx = idx;
    }

    /**
     * Creates a new instance of a ElasticSearch client.
     */
    public Client buildEsClient() {
      try {
        Settings settings = Settings.builder().put("cluster.name", cluster).build();
        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
      } catch (UnknownHostException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

  private DataRepoConfiguration dataRepoConfiguration;

  private ElasticSearch elasticSearch;

  private ServiceConfiguration service;

  private long storageCapacity;

  @JsonProperty
  public DataRepoConfiguration getDataRepoConfiguration() {
    return dataRepoConfiguration;
  }

  public void setDataRepoConfiguration(DataRepoConfiguration dataRepoConfiguration) {
    this.dataRepoConfiguration = dataRepoConfiguration;
  }

  @JsonProperty
  public ElasticSearch getElasticSearch() {
    return elasticSearch;
  }

  public void setElasticSearch(ElasticSearch elasticSearch) {
    this.elasticSearch = elasticSearch;
  }

  @JsonProperty
  public ServiceConfiguration getService() {
    return service;
  }

  public void setService(ServiceConfiguration service) {
    this.service = service;
  }

  @JsonProperty
  public long getStorageCapacity() {
    return storageCapacity;
  }

  public void setStorageCapacity(long storageCapacity) {
    this.storageCapacity = storageCapacity;
  }

  /**
   * DataOne implementation Tier.
   * This service implement up to tier 4.
   */
  public Tier getTier() {
    return Tier.TIER4;
  }

  @Override
  public LoggingFactory getLoggingFactory() {
    return LOGGING_FACTORY;
  }

  /**
   * https://github.com/dropwizard/dropwizard/issues/1567
   * Override getLoggingFactory for your configuration
   */
  private static class LogbackAutoConfigLoggingFactory implements LoggingFactory {

    @JsonIgnore
    private LoggerContext loggerContext;
    @JsonIgnore
    private final ContextInitializer contextInitializer;

    public LogbackAutoConfigLoggingFactory() {
      loggerContext = LoggingUtil.getLoggerContext();
      contextInitializer = new ContextInitializer(loggerContext);
    }

    @Override
    public void configure(MetricRegistry metricRegistry, String name) {
      try {
        contextInitializer.autoConfig();
      } catch (JoranException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void stop() {
      loggerContext.stop();
    }
  }
}
