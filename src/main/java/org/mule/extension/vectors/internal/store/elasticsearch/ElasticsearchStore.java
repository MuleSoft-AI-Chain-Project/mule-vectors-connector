package org.mule.extension.vectors.internal.store.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.*;
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
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

import java.io.IOException;
import java.util.*;

public class ElasticsearchStore extends BaseStore {

  static final String TEXT_DEFAULT_FIELD_NAME = "text";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "vector";

  private final String url;
  private final String user;
  private final String password;
  private final String apiKey;

  private RestClient restClient;

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
        .restClient(restClient)
        .indexName(storeName)
        .build();
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

    private ElasticsearchClient client;
    private String scrollId;
    private List<Hit<Map>> currentBatch;
    private int currentIndex;

    public RowIterator() throws IOException {
      super();
      this.client = new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
      this.currentBatch = new ArrayList<>();
      this.currentIndex = 0;
      fetchNextBatch();
    }

    @Override
    public boolean hasNext() {
      return currentIndex < currentBatch.size() || fetchNextBatch();
    }

    @Override
    public Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Hit<Map> hit = currentBatch.get(currentIndex++);
      String embeddingId = hit.id();
      Map<String, Object> sourceMap = hit.source();
      float[] vector = null;
      if (queryParams.retrieveEmbeddings()) {
        List<Double> vectorList = (List<Double>) sourceMap.get(VECTOR_DEFAULT_FIELD_NAME);
        vector = new float[vectorList.size()];
        for (int i = 0; i < vectorList.size(); i++) {
          vector[i] = vectorList.get(i).floatValue();
        }
      }
      String text = (String) sourceMap.get(TEXT_DEFAULT_FIELD_NAME);
      JSONObject metadataObject = new JSONObject((Map) sourceMap.get(METADATA_DEFAULT_FIELD_NAME));

      return new Row<TextSegment>(embeddingId,
                                  vector != null ? new Embedding(vector) : null,
                                  new TextSegment(text, Metadata.from(metadataObject.toMap())));
    }

    private boolean fetchNextBatch() {
      try {
        if (scrollId == null) {
          SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
              .index(storeName)
              .size(queryParams.pageSize())
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
      } catch (IOException e) {
        LOGGER.error("Error while fetching next batch", e);
        return false;
      }
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
