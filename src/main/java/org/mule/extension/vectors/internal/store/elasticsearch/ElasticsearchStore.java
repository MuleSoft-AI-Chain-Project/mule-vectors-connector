package org.mule.extension.vectors.internal.store.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchStore extends BaseStore {

  private final String url;
  private final String user;
  private final String password;
  private final String apiKey;

  private RestClient restClient;

  private RestClient getRestClient() {

    if(this.restClient == null) {

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
    }
    return restClient;
  }

  public ElasticsearchStore(StoreConfiguration storeConfiguration, ElasticsearchStoreConnection elasticsearchStoreConnection, String storeName, QueryParameters queryParams) {

    super(storeConfiguration, elasticsearchStoreConnection, storeName, queryParams, 0, true);

    this.url = elasticsearchStoreConnection.getUrl();
    this.user = elasticsearchStoreConnection.getUser();
    this.password = elasticsearchStoreConnection.getPassword();
    this.apiKey = elasticsearchStoreConnection.getApiKey();
    this.restClient = elasticsearchStoreConnection.getRestClient();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    return ElasticsearchEmbeddingStore.builder()
        .restClient(getRestClient())
        .indexName(storeName)
        .build();
  }

  public JSONObject listSources() {

    HashMap<String, JSONObject> sourceObjectMap = new HashMap<>();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);

    long segmentCount = 0; // Counter to track the number of segments processed

    // Create the Elasticsearch transport and client
    RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    ElasticsearchClient client = new ElasticsearchClient(transport);

    String scrollId = null;

    try {

        // Initial search request with scroll
        SearchRequest searchRequest = new SearchRequest.Builder()
            .index(storeName)
            .size(1)
            .scroll(Time.of(t -> t.time("1m")))
            .source(s -> s.filter(f -> f.includes("metadata")))
            .build();

        SearchResponse<Map> searchResponse = client.search(searchRequest, Map.class);

        // Process initial batch
        processHits(searchResponse.hits().hits(), sourceObjectMap);
        scrollId = searchResponse.scrollId();

        // Continue scrolling
        while (!searchResponse.hits().hits().isEmpty()) {
          ScrollRequest scrollRequest = new ScrollRequest.Builder()
              .scrollId(scrollId)
              .scroll(Time.of(t -> t.time("1m")))
              .build();

          ScrollResponse<Map> scrollResponse = client.scroll(scrollRequest, Map.class);
          processHits(scrollResponse.hits().hits(), sourceObjectMap);
          scrollId = scrollResponse.scrollId();

          if (scrollResponse.hits().hits().isEmpty()) {
            break;
          }
        }

    } catch (IOException e) {

      LOGGER.error("Error while listing sources", e);

    } finally {

      cleanup(client, scrollId);
    }

    jsonObject.put(Constants.JSON_KEY_SOURCES, JsonUtils.jsonObjectCollectionToJsonArray(sourceObjectMap.values()));
    jsonObject.put(Constants.JSON_KEY_SOURCE_COUNT, sourceObjectMap.size());

    return jsonObject;
  }

  private void processHits(List<Hit<Map>> hits, HashMap<String, JSONObject> sourceObjectMap) {

    for (Hit<Map> hit : hits) {

      // Convert the source map to a JSONObject
      Map<String, Object> sourceMap = hit.source();
      if (sourceMap != null) {
        JSONObject jsonObject = new JSONObject(sourceMap);
        JSONObject metadataObject = jsonObject.optJSONObject("metadata");
        if (metadataObject != null) {

          JSONObject sourceObject = getSourceObject(metadataObject);
          addOrUpdateSourceObjectIntoSourceObjectMap(sourceObjectMap, sourceObject);
        }
      }
    }
  }

  private void cleanup(ElasticsearchClient client, String scrollId) {

    if (scrollId != null) {

      try {

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest.Builder()
            .scrollId(scrollId)
            .build();
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest);
        if (!clearScrollResponse.succeeded()) {
          LOGGER.warn("Failed to clear scroll context");
        }
      } catch (IOException e) {

        LOGGER.error("Failed to clear scroll context");
      }
    }
  }

  @Override
  public ElasticsearchStore.RowIterator rowIterator() {
    try {
      return new ElasticsearchStore.RowIterator();
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
