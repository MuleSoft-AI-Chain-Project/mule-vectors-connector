package org.mule.extension.vectors.internal.store.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.ClearScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ElasticsearchStore extends BaseStoreService {

  static final String TEXT_DEFAULT_FIELD_NAME = "text";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "vector";

  private final String url;
  private final String user;
  private final String password;
  private final String apiKey;

  private final RestClient restClient;
  private final QueryParameters queryParams;

  public ElasticsearchStore(StoreConfiguration storeConfiguration, ElasticsearchStoreConnection elasticsearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, elasticsearchStoreConnection, storeName, dimension, createStore);

    this.url = elasticsearchStoreConnection.getUrl();
    this.user = elasticsearchStoreConnection.getUser();
    this.password = elasticsearchStoreConnection.getPassword();
    this.apiKey = elasticsearchStoreConnection.getApiKey();
    this.restClient = elasticsearchStoreConnection.getRestClient();
    this.queryParams = queryParams;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      return ElasticsearchEmbeddingStore.builder()
              .restClient(restClient)
              .indexName(storeName)
              .build();
    } catch (Exception e) {
      throw new ModuleException("Failed to build Elasticsearch embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public Iterator<BaseStoreService.Row<?>> getRowIterator() {
    try {
      return new ElasticsearchStore.RowIterator();
    } catch (ElasticsearchException e) {
      LOGGER.error("Elasticsearch error while creating row iterator", e);
      throw new ModuleException("Elasticsearch service error: " + e.getMessage(), MuleVectorsErrorType.SERVICE_ERROR, e);
    } catch (ConnectException e) {
      throw new ModuleException("Connection failed: " + e.getMessage(), MuleVectorsErrorType.CONNECTION_FAILED, e);
    } catch (IOException e) {
      throw new ModuleException("Network error while creating Elasticsearch iterator: " + e.getMessage(), MuleVectorsErrorType.NETWORK_ERROR, e);
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new ModuleException("Failed to create Elasticsearch iterator: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

    private final ElasticsearchClient client;
    private String scrollId;
    private List<Hit<Map>> currentBatch;
    private int currentIndex;

    public RowIterator() throws IOException {
      this.client = new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
      this.currentBatch = new ArrayList<>();
      this.currentIndex = 0;
      fetchNextBatch();
    }

    @Override
    public boolean hasNext() {
      try {
        return currentIndex < currentBatch.size() || fetchNextBatch();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public BaseStoreService.Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Hit<Map> hit = currentBatch.get(currentIndex++);
      String embeddingId = hit.id();
      Map<String, Object> sourceMap = hit.source();
      float[] vector = null;
      if (queryParams.retrieveEmbeddings()) {
        List<Double> vectorList = (List<Double>) sourceMap.get(VECTOR_DEFAULT_FIELD_NAME);
        if (vectorList != null) {
          vector = new float[vectorList.size()];
          for (int i = 0; i < vectorList.size(); i++) {
            vector[i] = vectorList.get(i).floatValue();
          }
        }
      }
      String text = (String) sourceMap.get(TEXT_DEFAULT_FIELD_NAME);
      JSONObject metadataObject = new JSONObject((Map) sourceMap.get(METADATA_DEFAULT_FIELD_NAME));

      return new BaseStoreService.Row<TextSegment>(embeddingId,
                                  vector != null ? new Embedding(vector) : null,
                                  new TextSegment(text, Metadata.from(metadataObject.toMap())));
    }

    private boolean fetchNextBatch() throws IOException {
      if (scrollId == null) {
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .index(storeName)
                .size((int) queryParams.pageSize())
                .scroll(Time.of(t -> t.time("1m")));

        if (queryParams.retrieveEmbeddings()) {
          searchRequestBuilder.source(s -> s.filter(f -> f.includes(TEXT_DEFAULT_FIELD_NAME,
                  METADATA_DEFAULT_FIELD_NAME,
                  VECTOR_DEFAULT_FIELD_NAME)));
        } else {
          searchRequestBuilder.source(s -> s.filter(f -> f.includes(TEXT_DEFAULT_FIELD_NAME,
                  METADATA_DEFAULT_FIELD_NAME)));
        }

        SearchRequest searchRequest = searchRequestBuilder.build();
        SearchResponse<Map> searchResponse = client.search(searchRequest, Map.class);
        currentBatch = searchResponse.hits().hits();
        scrollId = searchResponse.scrollId();
      } else {
        ScrollRequest scrollRequest = new ScrollRequest.Builder()
                .scrollId(scrollId)
                .scroll(Time.of(t -> t.time("1m")))
                .build();
        ScrollResponse<Map> scrollResponse = client.scroll(scrollRequest, Map.class);
        currentBatch = scrollResponse.hits().hits();
        scrollId = scrollResponse.scrollId();
      }
      currentIndex = 0;
      if (currentBatch.isEmpty()) {
        close();
        return false;
      }
      return true;
    }

    public void close() {
      if (scrollId != null) {
        try {
          ClearScrollRequest clearScrollRequest = new ClearScrollRequest.Builder()
                  .scrollId(scrollId)
                  .build();
          client.clearScroll(clearScrollRequest);
        } catch (IOException e) {
          LOGGER.error("Failed to clear scroll context", e);
        }
      }
    }
  }
}
