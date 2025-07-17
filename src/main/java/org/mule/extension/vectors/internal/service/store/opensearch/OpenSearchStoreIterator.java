package org.mule.extension.vectors.internal.service.store.opensearch;


import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

import java.io.IOException;
import java.util.*;

public class OpenSearchStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchStoreIterator.class);

  private final String storeName;
  private final QueryParameters queryParams;
  private final OpenSearchClient openSearchClient;
  private final OpenSearchStoreConnection OpenSearchStoreConnection;

  private String scrollId;
  private List<Hit<Map<String, Object>>> currentBatch;
  private int currentIndex;

  public OpenSearchStoreIterator(
      OpenSearchStoreConnection openSearchStoreConnection,
      String storeName,
      QueryParameters queryParams
  )  {
    this.OpenSearchStoreConnection = openSearchStoreConnection;
    this.openSearchClient = openSearchStoreConnection.getOpenSearchClient();
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.currentBatch = new ArrayList<>();
    this.currentIndex = 0;
    try {
      fetchNextBatch();
    } catch (IOException e) {
      throw new ModuleException("Store issue",MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public boolean hasNext() {
    try {
      return currentIndex < currentBatch.size() || fetchNextBatch();
    } catch (IOException e) {
      throw new ModuleException("Error fetching next batch from OpenSearch", MuleVectorsErrorType.SERVICE_ERROR, e);
    }
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    Hit<Map<String, Object>> hit = currentBatch.get(currentIndex++);
    String embeddingId = hit.id();
    Map<String, Object> sourceMap = hit.source();
    String VECTOR_DEFAULT_FIELD_NAME = "vector";
    String TEXT_DEFAULT_FIELD_NAME = "text";
    String METADATA_DEFAULT_FIELD_NAME = "metadata";
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

    // This is the only place you may want to adapt for Embedded type.
    // If you want to keep it generic, you can cast or use a factory.
    // For now, we keep it as TextSegment to match the original.
    @SuppressWarnings("unchecked")
    Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataObject.toMap()));

    return new VectorStoreRow<>(embeddingId,
                                vector != null ? new Embedding(vector) : null,
                                embedded);
  }

  private boolean fetchNextBatch() throws IOException {
    String VECTOR_DEFAULT_FIELD_NAME = "vector";
    String TEXT_DEFAULT_FIELD_NAME = "text";
    String METADATA_DEFAULT_FIELD_NAME = "metadata";

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
      SearchResponse<Map<String, Object>> searchResponse = openSearchClient.search(searchRequest, (Class<Map<String, Object>>)(Class<?>)Map.class);
      currentBatch = searchResponse.hits().hits();
      scrollId = searchResponse.scrollId();
    } else {
      ScrollRequest scrollRequest = new ScrollRequest.Builder()
          .scrollId(scrollId)
          .scroll(Time.of(t -> t.time("1m")))
          .build();
      ScrollResponse<Map<String, Object>> scrollResponse = openSearchClient.scroll(scrollRequest, (Class<Map<String, Object>>)(Class<?>)Map.class);
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
        openSearchClient.clearScroll(builder -> builder.scrollId(scrollId));
      } catch (IOException e) {
        LOGGER.error("Failed to clear scroll context", e);
      }
    }
  }
}
