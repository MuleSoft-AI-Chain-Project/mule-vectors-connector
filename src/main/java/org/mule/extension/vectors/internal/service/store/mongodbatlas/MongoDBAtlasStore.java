package org.mule.extension.vectors.internal.service.store.mongodbatlas;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoSecurityException;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.HashSet;
public class MongoDBAtlasStore extends BaseStoreService {

  private final MongoClient mongoClient;
  private final QueryParameters queryParams;

  public MongoDBAtlasStore(StoreConfiguration storeConfiguration, MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, mongoDBAtlasStoreConnection, storeName, dimension, createStore);
    this.mongoClient = mongoDBAtlasStoreConnection.getMongoClient();
    this.queryParams = queryParams;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      // Create embedding store with automatic index creation
      MongoDbEmbeddingStore.Builder embeddingStoreBuilder = MongoDbEmbeddingStore.builder()
              .databaseName(((MongoDBAtlasStoreConnection) storeConnection).getDatabase())
              .collectionName(storeName)
              .createIndex(createStore)
              .indexName("vector_index")
              .fromClient(mongoClient);

      if (createStore) {

        // Configure index mapping for vector search
        IndexMapping indexMapping = IndexMapping.builder()
                .dimension(dimension)
                .metadataFieldNames(new HashSet<>())
                .build();

        embeddingStoreBuilder.indexMapping(indexMapping);
      }

      return embeddingStoreBuilder.build();
    } catch (MongoSecurityException e) {
      throw new ModuleException("MongoDB authentication failed: " + e.getMessage(), MuleVectorsErrorType.AUTHENTICATION, e);
    } catch (MongoCommandException e) {
      throw new ModuleException("MongoDB command failed: " + e.getErrorMessage(), MuleVectorsErrorType.INVALID_REQUEST, e);
    } catch (MongoException e) {
      throw new ModuleException("Failed to build MongoDB embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public MongoDBAtlasStoreIterator<?> getFileIterator() {
    return new MongoDBAtlasStoreIterator<>(
      (MongoDBAtlasStoreConnection) this.storeConnection,
      this.storeName,
      this.queryParams
    );
  }
}
