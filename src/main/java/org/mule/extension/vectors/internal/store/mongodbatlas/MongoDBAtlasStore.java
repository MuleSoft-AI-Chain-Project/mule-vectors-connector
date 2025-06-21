package org.mule.extension.vectors.internal.store.mongodbatlas;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.*;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
  public Iterator<BaseStoreService.Row<?>> getRowIterator() {
    try {
      return new MongoDBAtlasStore.RowIterator();
    } catch (ModuleException e) {
      throw e; // Re-throw our own exceptions
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new ModuleException("Failed to create MongoDB iterator: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

    private static final String ID_FIELD = "_id";
    private static final String METADATA_FIELD = "metadata";
    private static final String TEXT_FIELD = "text";
    private static final String VECTOR_FIELD = "embedding";

    private final com.mongodb.client.MongoCollection<org.bson.Document> collection;
    private final int pageSize;
    private int skip;
    private List<org.bson.Document> currentBatch;
    private int currentIndex;
    private boolean noMoreData;

    public RowIterator() {
      this.collection = mongoClient.getDatabase(((MongoDBAtlasStoreConnection)storeConnection).getDatabase())
        .getCollection(storeName);
      this.pageSize = (int) queryParams.pageSize();
      this.skip = 0;
      this.currentBatch = new ArrayList<>();
      this.currentIndex = 0;
      this.noMoreData = false;
      loadNextBatch();
    }

    private void loadNextBatch() {
      try {
        currentBatch.clear();
        currentIndex = 0;
        List<org.bson.Document> batch = collection.find()
                .skip(skip)
                .limit(pageSize)
                .into(new ArrayList<>());
        if (batch.isEmpty()) {
          noMoreData = true;
        } else {
          currentBatch.addAll(batch);
          skip += batch.size();
        }
      } catch (MongoSocketOpenException | MongoSocketReadException e) {
        throw new ModuleException("MongoDB connection failed: " + e.getMessage(), MuleVectorsErrorType.CONNECTION_FAILED, e);
      } catch (MongoSecurityException e) {
        throw new ModuleException("MongoDB authentication failed: " + e.getMessage(), MuleVectorsErrorType.AUTHENTICATION, e);
      } catch (MongoCommandException e) {
        throw new ModuleException("MongoDB query failed: " + e.getErrorMessage(), MuleVectorsErrorType.INVALID_REQUEST, e);
      } catch (MongoException e) {
        throw new ModuleException("Error fetching from MongoDB: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
      }
    }

    @Override
    public boolean hasNext() {
      if (currentIndex < currentBatch.size()) {
        return true;
      } else if (!noMoreData) {
        loadNextBatch();
        return currentIndex < currentBatch.size();
      } else {
        return false;
      }
    }

    @Override
    public BaseStoreService.Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      org.bson.Document doc = currentBatch.get(currentIndex++);
      String id = doc.get(ID_FIELD).toString();
      org.bson.Document metadataDoc = doc.get(METADATA_FIELD, org.bson.Document.class);
      String text = doc.getString(TEXT_FIELD);
      float[] vector = null;
      if (queryParams.retrieveEmbeddings() && doc.containsKey(VECTOR_FIELD)) {
        List<Double> vectorList = doc.getList(VECTOR_FIELD, Double.class);
        vector = new float[vectorList.size()];
        for (int i = 0; i < vectorList.size(); i++) {
          vector[i] = vectorList.get(i).floatValue();
        }
      }
      return new BaseStoreService.Row<>(
        id,
        vector != null ? new Embedding(vector) : null,
        new TextSegment(text, Metadata.from(metadataDoc))
      );
    }
  }
}
