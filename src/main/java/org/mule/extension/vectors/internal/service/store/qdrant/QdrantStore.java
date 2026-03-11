package org.mule.extension.vectors.internal.service.store.qdrant;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.List;
import java.util.concurrent.ExecutionException;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.VectorOutput;
import io.qdrant.client.grpc.Points.VectorsOutput;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QdrantStore extends BaseStoreService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QdrantStore.class);

  private final String payloadTextKey;
  private final QdrantClient client;
  private final QueryParameters queryParams;

  public QdrantStore(StoreConfiguration storeConfiguration, QdrantStoreConnection qdrantStoreConnection, String storeName,
                     QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, qdrantStoreConnection, storeName, dimension, createStore);


    this.client = qdrantStoreConnection.getClient();
    this.queryParams = queryParams;
    try {
      if (createStore && !this.client.collectionExistsAsync(this.storeName).get() && dimension > 0) {

        qdrantStoreConnection.createCollection(storeName, dimension);
      }
      this.payloadTextKey = qdrantStoreConnection.getTextSegmentKey();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ModuleException("Qdrant API request failed: ", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    } catch (Exception e) {
      throw new ModuleException("Qdrant API request failed: ", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    try {
      return QdrantEmbeddingStore.builder()
          .client(client)
          .payloadTextKey(payloadTextKey)
          .collectionName(storeName)
          .build();
    } catch (Exception e) {
      throw new ModuleException("Failed to build Qdrant embedding store: " + e.getMessage(),
                                MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public JSONObject query(List<TextSegment> textSegments,
                          List<Embedding> embeddings,
                          Number maxResults,
                          Double minScore,
                          SearchFilterParameters searchFilterParams) {

    LOGGER.debug("Qdrant query on collection '{}': queryEmbeddingDim={}, maxResults={}, minScore={}",
                 storeName, embeddings.get(0).vector().length, maxResults, minScore);
    try {
      return super.query(textSegments, embeddings, maxResults, minScore, searchFilterParams);
    } catch (Exception e) {
      logQdrantVectorDiagnostics(embeddings.get(0));
      throw e;
    }
  }

  private void logQdrantVectorDiagnostics(Embedding queryEmbedding) {
    try {
      SearchPoints searchRequest = SearchPoints.newBuilder()
          .setCollectionName(storeName)
          .addAllVector(queryEmbedding.vectorAsList())
          .setLimit(1)
          .setWithVectors(WithVectorsSelectorFactory.enable(true))
          .build();
      List<ScoredPoint> results = client.searchAsync(searchRequest).get();
      if (!results.isEmpty()) {
        ScoredPoint point = results.get(0);
        VectorsOutput vectors = point.getVectors();
        VectorOutput vectorOutput = vectors.getVector();
        LOGGER.error("Qdrant vector diagnostics for collection '{}': "
            + "vectorsOptionsCase={}, vectorOutputCase={}, "
            + "legacyDataListSize={}, denseDataListSize={}",
                     storeName,
                     vectors.getVectorsOptionsCase(),
                     vectorOutput.getVectorCase(),
                     vectorOutput.getDataList().size(),
                     vectorOutput.getDense().getDataList().size());
      } else {
        LOGGER.error("Qdrant vector diagnostics for collection '{}': no results returned from direct search", storeName);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Interrupted while collecting Qdrant vector diagnostics for collection '{}'", storeName, e);
    } catch (ExecutionException e) {
      LOGGER.error("Failed to collect Qdrant vector diagnostics for collection '{}'", storeName, e);
    }
  }

  @Override
  public QdrantStoreIterator<?> getFileIterator() {
    return new QdrantStoreIterator<>(
                                     (QdrantStoreConnection) this.storeConnection,
                                     this.storeName,
                                     this.queryParams);
  }
}

