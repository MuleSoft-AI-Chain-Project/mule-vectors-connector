package org.mule.extension.vectors.internal.store.mongodbatlas;


import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mule.extension.vectors.internal.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;

import java.util.*;

public class MongoDBAtlasStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBAtlasStoreIterator.class);

  private final MongoClient mongoClient;
  private final String storeName;
  private final QueryParameters queryParams;
  private final String databaseName;

  private final com.mongodb.client.MongoCollection<org.bson.Document> collection;
  private final int pageSize;
  private final MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection;
  private int skip;
  private List<org.bson.Document> currentBatch;
  private int currentIndex;
  private boolean noMoreData;

  public MongoDBAtlasStoreIterator(
      MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) {
    this.mongoClient = mongoDBAtlasStoreConnection.getMongoClient();
    this.databaseName = mongoDBAtlasStoreConnection.getDatabase();
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.collection = mongoClient.getDatabase(databaseName).getCollection(storeName);
    this.pageSize = (int) queryParams.pageSize();
    this.skip = 0;
    this.currentBatch = new ArrayList<>();
    this.currentIndex = 0;
    this.noMoreData = false;
    this.mongoDBAtlasStoreConnection = mongoDBAtlasStoreConnection;
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
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    org.bson.Document doc = currentBatch.get(currentIndex++);
    String ID_FIELD = "_id";
    String METADATA_FIELD = "metadata";
    String TEXT_FIELD = "text";
    String VECTOR_FIELD = "embedding";

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

    // This is the only place you may want to adapt for Embedded type.
    // If you want to keep it generic, you can cast or use a factory.
    // For now, we keep it as TextSegment to match the original.
    @SuppressWarnings("unchecked")
    Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataDoc));

    return new VectorStoreRow<>(
        id,
        vector != null ? new Embedding(vector) : null,
        embedded
    );
  }
}
