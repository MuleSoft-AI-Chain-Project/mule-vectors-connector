package org.mule.extension.vectors.internal.store.mongodbatlas;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.dml.QueryIteratorParam;
import io.milvus.response.QueryResultsWrapper;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

import java.util.*;

public class MongoDBAtlasStore extends BaseStore {

  private MongoClient mongoClient;

  public MongoDBAtlasStore(StoreConfiguration storeConfiguration, MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, mongoDBAtlasStoreConnection, storeName, queryParams, dimension, createStore);
    this.mongoClient = mongoDBAtlasStoreConnection.getMongoClient();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    // Create embedding store with automatic index creation
    MongoDbEmbeddingStore.Builder embeddingStoreBuilder = MongoDbEmbeddingStore.builder()
        .databaseName(((MongoDBAtlasStoreConnection)storeConnection).getDatabase())
        .collectionName(storeName)
        .createIndex(createStore)
        .indexName("vector_index")
        .fromClient(mongoClient);

    if(createStore){

      // Configure index mapping for vector search
      IndexMapping indexMapping = IndexMapping.builder()
          .dimension(dimension)
          .metadataFieldNames(new HashSet<>())
          .build();

      embeddingStoreBuilder.indexMapping(indexMapping);
    }

    return embeddingStoreBuilder.build();
  }

  @Override
  public MongoDBAtlasStore.RowIterator rowIterator() {
    try {
      return new MongoDBAtlasStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {

    private QueryIterator queryIterator;
    private List<QueryResultsWrapper.RowRecord> currentBatch;
    private int currentIndex;

    public RowIterator() throws Exception {
        super();
    }

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public Row<?> next() { return null; }
  }
}
