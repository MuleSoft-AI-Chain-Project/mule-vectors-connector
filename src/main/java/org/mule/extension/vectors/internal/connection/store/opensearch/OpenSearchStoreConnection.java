package org.mule.extension.vectors.internal.connection.store.opensearch;

import dev.langchain4j.internal.Utils;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

public class OpenSearchStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchStoreConnection.class);

  private String url;
  private String user;
  private String password;
  private String apiKey;
  
  private OpenSearchClient openSearchClient;

  private final OpenSearchStoreConnectionParameters parameters;

  public OpenSearchStoreConnection(final OpenSearchStoreConnectionParameters openSearchStoreConnectionParameters) {
    this.parameters = openSearchStoreConnectionParameters;
    this.url = openSearchStoreConnectionParameters.getUrl();
    this.user = openSearchStoreConnectionParameters.getUser();
    this.password = openSearchStoreConnectionParameters.getPassword();
    this.apiKey = openSearchStoreConnectionParameters.getApiKey();
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

  public OpenSearchClient getOpenSearchClient() {
    return openSearchClient;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_OPENSEARCH;
  }

  public void initialise() throws URISyntaxException {

      HttpHost openSearchHost = HttpHost.create(url);

      OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(new HttpHost[]{openSearchHost}).setMapper(new JacksonJsonpMapper()).setHttpClientConfigCallback((httpClientBuilder) -> {

        if (!Utils.isNullOrBlank(apiKey)) {
          httpClientBuilder.setDefaultHeaders(Collections.singletonList(new BasicHeader("Authorization", "ApiKey " + apiKey)));
        }

        if (!Utils.isNullOrBlank(user) && !Utils.isNullOrBlank(password)) {
          BasicCredentialsProvider
              credentialsProvider = new BasicCredentialsProvider();
          credentialsProvider.setCredentials(new org.apache.hc.client5.http.auth.AuthScope(openSearchHost), new org.apache.hc.client5.http.auth.UsernamePasswordCredentials(user, password.toCharArray()));
          httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        httpClientBuilder.setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create().build());
          return httpClientBuilder;
        }).build();

      this.openSearchClient = new OpenSearchClient(transport);

  }

  @Override
  public void disconnect() {

    try {
      // Add logic here

    } catch (Exception e) {

      LOGGER.error("Failed to close connection to OpenSearch.", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public OpenSearchStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    if (parameters.getUrl() == null || parameters.getUrl().isBlank()) {
      throw new IllegalArgumentException("URL is required for OpenSearch connection");
    }
    if ((parameters.getPassword() == null || parameters.getPassword().isBlank()) && (parameters.getApiKey() == null || parameters.getApiKey().isBlank())) {
      throw new IllegalArgumentException("Either password or API Key is required for OpenSearch connection");
    }
    try {
      this.openSearchClient.ping();
    } catch (Exception e) {
      throw new ModuleException("Failed to validate connection to OpenSearch.", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }
}
