package org.mule.extension.vectors.internal.service.store.elasticsearch;


import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.ClearScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.*;

public class ElasticsearchStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchStoreIterator.class);

  private final String storeName;
  private final QueryParameters queryParams;
  private final RestClient restClient;

  private ElasticsearchClient client;
  private String scrollId;
  private List<Hit<Map>> currentBatch;
  private int currentIndex;

  public ElasticsearchStoreIterator(
      ElasticsearchStoreConnection elasticsearchStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) {
    this.restClient = elasticsearchStoreConnection.getRestClient();
    this.storeName = storeName;
    this.queryParams = queryParams;
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
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    Hit<Map> hit = currentBatch.get(currentIndex++);
    String embeddingId = hit.id();
    Map<String, Object> sourceMap = hit.source();
    float[] vector = null;
    String VECTOR_DEFAULT_FIELD_NAME = "vector";
    String TEXT_DEFAULT_FIELD_NAME = "text";
    String METADATA_DEFAULT_FIELD_NAME = "metadata";

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

    // This is the only place you may want to adapt for Embedded type.
    // If you want to keep it generic, you can cast or use a factory.
    // For now, we keep it as TextSegment to match the original.
    @SuppressWarnings("unchecked")
    Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataObject.toMap()));

    return new VectorStoreRow<>(embeddingId,
                                vector != null ? new Embedding(vector) : null,
                                embedded);
  }

  private boolean fetchNextBatch()  {
    String VECTOR_DEFAULT_FIELD_NAME = "vector";
    String TEXT_DEFAULT_FIELD_NAME = "text";
    String METADATA_DEFAULT_FIELD_NAME = "metadata";
try {


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
} catch (IOException e){
  throw new ModuleException("Error", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
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
