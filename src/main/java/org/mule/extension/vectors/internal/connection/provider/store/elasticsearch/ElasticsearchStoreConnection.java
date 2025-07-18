package org.mule.extension.vectors.internal.connection.provider.store.elasticsearch;

import dev.langchain4j.internal.Utils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;

import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
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

      throw new ModuleException("Failed to close connection to Elasticsearch.", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
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
      throw new ModuleException("URL is required for Elasticsearch connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    if ((parameters.getPassword() == null || parameters.getPassword().isBlank()) && (parameters.getApiKey() == null || parameters.getApiKey().isBlank())) {
      throw new ModuleException("Either password or API Key is required for Elasticsearch connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }  
    try {
      this.restClient.getNodes();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Elastic search", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }
  public void initialise() throws IOException {

    if (!Utils.isNullOrBlank(user)) {

      BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
      credsProv.setCredentials(
          AuthScope.ANY, new UsernamePasswordCredentials(user, password)
      );

      this.restClient = RestClient
          .builder(HttpHost.create(url))
          .setHttpClientConfigCallback(hc -> hc
              .setDefaultCredentialsProvider(credsProv)
          )
          .build();

    } else if (!Utils.isNullOrBlank(apiKey)) {

      this.restClient = RestClient
          .builder(HttpHost.create(url))
          .setDefaultHeaders(new Header[] {
              new BasicHeader("Authorization", "ApiKey " + apiKey)
          })
          .build();
    }


    // Create the transport with a Jackson mapper
    ElasticsearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper());

    // And create the API client
    ElasticsearchClient esClient = new ElasticsearchClient(transport);
    if(!esClient.ping().value()) {

      throw new IOException("Impossible to connect to Elasticsearch.");
    }
  }
}
