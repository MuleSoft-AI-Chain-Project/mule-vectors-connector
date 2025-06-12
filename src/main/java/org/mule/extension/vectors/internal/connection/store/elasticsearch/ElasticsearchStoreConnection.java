package org.mule.extension.vectors.internal.connection.store.elasticsearch;

import dev.langchain4j.internal.Utils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticsearchStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchStoreConnection.class);

  private String url;
  private String user;
  private String password;
  private String apiKey;

  private RestClient restClient;

  private final ElasticsearchStoreConnectionParameters parameters;

  public ElasticsearchStoreConnection(ElasticsearchStoreConnectionParameters parameters) {
    this.parameters = parameters;
    this.url = parameters.getUrl();
    this.user = parameters.getUser();
    this.password = parameters.getPassword();
    this.apiKey = parameters.getApiKey();
  }

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getApiKey() {
    return apiKey;
  }

  public RestClient getRestClient() {
    return restClient;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_ELASTICSEARCH;
  }

  @Override
  public void disconnect() {

    try {
      this.restClient.close();

    } catch (IOException e) {

      LOGGER.error("Failed to close connection to Elasticsearch.", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    if (parameters.getUrl() == null || parameters.getUrl().isBlank()) {
      throw new IllegalArgumentException("URL is required for Elasticsearch connection");
    }
    if ((parameters.getPassword() == null || parameters.getPassword().isBlank()) && (parameters.getApiKey() == null || parameters.getApiKey().isBlank())) {
      throw new IllegalArgumentException("Either password or API Key is required for Elasticsearch connection");
    }
  }
}
