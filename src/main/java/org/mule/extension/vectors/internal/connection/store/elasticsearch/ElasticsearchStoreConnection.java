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

  public ElasticsearchStoreConnection(String url, String userName, String password, String apikey) {
    this.url = url;
    this.user = userName;
    this.password = password;
    this.apiKey = apikey;
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
  public void connect() throws ConnectionException{


    try {
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
            .setDefaultHeaders(new Header[]{
                new BasicHeader("Authorization", "ApiKey " + apiKey)
            })
            .build();
      }

      if(restClient == null) {

        throw new ConnectionException("Impossible to connect to Elasticsearch. Impossible to initialize the restClient");
      }

      // Create the transport with a Jackson mapper
      ElasticsearchTransport transport = new RestClientTransport(
          restClient, new JacksonJsonpMapper());

      // And create the API client
      ElasticsearchClient esClient = new ElasticsearchClient(transport);

      if(!esClient.ping().value()) {

        throw new ConnectionException("Impossible to connect to Elasticsearch.");
      }

    } catch (ConnectionException e) {

      throw e;
    } catch (IOException e) {

      throw new ConnectionException("Impossible to connect to Elasticsearch.", e);
    }

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
  public boolean isValid() {

    // Create the transport with a Jackson mapper
    ElasticsearchTransport transport = new RestClientTransport(
        this.restClient, new JacksonJsonpMapper());

    // And create the API client
    ElasticsearchClient esClient = new ElasticsearchClient(transport);

    try {

      return esClient.ping().value();

    } catch (IOException e) {

      LOGGER.error("Failed to validate connection to Elasticsearch.", e);
      return false;
    }
  }
}
