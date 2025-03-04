package org.mule.extension.vectors.internal.store.opensearch;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class OpenSearchStore extends BaseStore {

  private final String url;
  private final String user;
  private final String password;
  private final String apiKey;
  private OpenSearchClient openSearchClient;

  public OpenSearchClient getOpenSearchClient() {

    if(openSearchClient == null){

      HttpHost openSearchHost = null;
      try { openSearchHost = HttpHost.create(url); } catch (URISyntaxException e) {

        throw new RuntimeException(e);
      }
      HttpHost finalOpenSearchHost = openSearchHost;
      OpenSearchTransport
          transport = ApacheHttpClient5TransportBuilder.builder(new HttpHost[]{openSearchHost}).setMapper(new JacksonJsonpMapper()).setHttpClientConfigCallback((httpClientBuilder) -> {

        if (!Utils.isNullOrBlank(apiKey)) {
          httpClientBuilder.setDefaultHeaders(Collections.singletonList(new BasicHeader("Authorization", "ApiKey " + apiKey)));
        }

        if (!Utils.isNullOrBlank(user) && !Utils.isNullOrBlank(password)) {
          org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
              credentialsProvider = new org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider();
          credentialsProvider.setCredentials(new org.apache.hc.client5.http.auth.AuthScope(finalOpenSearchHost), new org.apache.hc.client5.http.auth.UsernamePasswordCredentials(user, password.toCharArray()));
          httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        httpClientBuilder.setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create().build());
        return httpClientBuilder;
      }).build();

      this.openSearchClient = new OpenSearchClient(transport);
    }
    return openSearchClient;
  }

  public OpenSearchStore(StoreConfiguration storeConfiguration, OpenSearchStoreConnection openSearchStoreConnection, String storeName, QueryParameters queryParams) {

    super(storeConfiguration, openSearchStoreConnection, storeName, queryParams, 0, true);

    this.url = openSearchStoreConnection.getUrl();
    this.user = openSearchStoreConnection.getUser();
    this.password = openSearchStoreConnection.getPassword();
    this.apiKey = openSearchStoreConnection.getApiKey();
    this.openSearchClient = openSearchStoreConnection.getOpenSearchClient();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

      return OpenSearchEmbeddingStore.builder()
          .serverUrl(url)
          .openSearchClient(openSearchClient)
          .indexName(storeName)
          .build();
  }

  public JSONObject listSources() {

    HashMap<String, JSONObject> sourceObjectMap = new HashMap<>();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);

    long segmentCount = 0; // Counter to track the number of segments processed

    try {

      String[] scrollId = new String[1];

      try {

        // Initial search request with scroll
        SearchRequest searchRequest = new SearchRequest.Builder()
            .index(storeName)
            .source(source -> source.filter(filter -> filter.includes("metadata"))) // Filter to include only metadata
            .size(queryParams.embeddingPageSize())
            .scroll(Time.of(t -> t.time("1m")))
            .build();

        SearchResponse<Object> searchResponse = openSearchClient.search(searchRequest, Object.class);

        // Process initial batch
        processHits(searchResponse.hits().hits(), sourceObjectMap);

        scrollId[0] = searchResponse.scrollId();

        // Continue scrolling
        while (!searchResponse.hits().hits().isEmpty()) {
          ScrollRequest scrollRequest = new ScrollRequest.Builder()
              .scrollId(scrollId[0])
              .scroll(Time.of(t -> t.time("1m")))
              .build();

          ScrollResponse<Object> scrollResponse = openSearchClient.scroll(scrollRequest, Object.class);
          processHits(scrollResponse.hits().hits(), sourceObjectMap);
          scrollId[0] = scrollResponse.scrollId();

          if (scrollResponse.hits().hits().isEmpty()) {
            break;
          }
        }

      } finally {
        cleanup(openSearchClient, scrollId[0]);
      }

    } catch (IOException e) {

      LOGGER.error("Error while listing sources", e);
    }

    jsonObject.put(Constants.JSON_KEY_SOURCES, JsonUtils.jsonObjectCollectionToJsonArray(sourceObjectMap.values()));
    jsonObject.put(Constants.JSON_KEY_SOURCE_COUNT, sourceObjectMap.size());

    return jsonObject;
  }

  private void processHits(List<Hit<Object>> hits, HashMap<String, JSONObject> sourceObjectMap) {

    for (Hit<Object> hit : hits) {

      // Convert the Map to a JSONObject
      JSONObject jsonObject = new JSONObject((Map<?, ?>) hit.source());
      // Extract the metadata field from the JSONObject
      JSONObject metadataObject = jsonObject.optJSONObject("metadata");
      if(metadataObject != null) {

        JSONObject sourceObject = getSourceObject(metadataObject);
        addOrUpdateSourceObjectIntoSourceObjectMap(sourceObjectMap, sourceObject);
      }
    }
  }

  private void cleanup(OpenSearchClient client, String scrollId) {
    if (scrollId != null) {
      try {
        client.clearScroll(builder -> builder.scrollId(scrollId));
      } catch (IOException e) {
        LOGGER.error("Error while cleaning up scroll when listing sources", e);
      }
    }
  }

  @Override
  public OpenSearchStore.RowIterator rowIterator() {
    try {
      return new OpenSearchStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {


    public RowIterator() throws Exception {

      super();
    }

    @Override
    public boolean hasNext() {

      return true;
    }

    @Override
    public Row<?> next() {
      try {

        return null;

      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        return null;
      }
    }
  }
}
