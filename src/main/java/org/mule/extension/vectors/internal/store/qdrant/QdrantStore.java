package org.mule.extension.vectors.internal.store.qdrant;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;

import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QdrantStore extends BaseStoreService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QdrantStore.class);

  private final String payloadTextKey;
  private final QdrantClient client;
  private final QueryParameters queryParams;

  public QdrantStore(StoreConfiguration storeConfiguration, QdrantStoreConnection qdrantStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, qdrantStoreConnection, storeName, dimension, createStore);


    this.client = qdrantStoreConnection.getClient();
    this.queryParams = queryParams;
try {
  if (createStore && !this.client.collectionExistsAsync(this.storeName).get() && dimension > 0) {

    qdrantStoreConnection.createCollection(storeName, dimension);
  }
  this.payloadTextKey = qdrantStoreConnection.getTextSegmentKey();
} catch (Exception e){
  throw new ModuleException("Qdrant API request failed: ",  MuleVectorsErrorType.STORE_SERVICES_FAILURE, e );
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
      throw new ModuleException("Failed to build Qdrant embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }
  }

