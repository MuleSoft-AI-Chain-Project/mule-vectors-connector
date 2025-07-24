package org.mule.extension.vectors.internal.service.store.alloydb;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.sql.SQLException;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEmbeddingStore;
import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import dev.langchain4j.community.store.embedding.alloydb.EmbeddingStoreConfig;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

public class AlloyDBStore extends BaseStoreService {

  static final String ID_DEFAULT_FIELD_NAME = "langchain_id";
  static final String TEXT_DEFAULT_FIELD_NAME = "content";
  static final String METADATA_DEFAULT_FIELD_NAME = "langchain_metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "embedding";

  private final AlloyDBEngine alloyDBEngine;
  private final QueryParameters queryParams;

  public AlloyDBStore(StoreConfiguration compositeConfiguration, AlloyDBStoreConnection alloyDBStoreConnection, String storeName,
                      QueryParameters queryParams, int dimension, boolean createStore) {

    super(compositeConfiguration, alloyDBStoreConnection, storeName, dimension, createStore);
    this.alloyDBEngine = alloyDBStoreConnection.getAlloyDBEngine();
    this.queryParams = queryParams;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      if (createStore) {

        EmbeddingStoreConfig embeddingStoreConfig = EmbeddingStoreConfig
            .builder(storeName, dimension)
            .overwriteExisting(false)
            .build();

        this.alloyDBEngine.initVectorStoreTable(embeddingStoreConfig);
      }

      return new AlloyDBEmbeddingStore.Builder(this.alloyDBEngine, storeName)
          .build();
    } catch (Exception e) {
      if (e.getCause() instanceof SQLException) {
        handleSQLException((SQLException) e.getCause());
      }
      throw new ModuleException("Failed to build AlloyDB embedding store: " + e.getMessage(),
                                MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public AlloyDBStoreIterator<?> getFileIterator() {
    return new AlloyDBStoreIterator<>(
                                      (AlloyDBStoreConnection) this.storeConnection,
                                      this.storeName,
                                      this.queryParams);
  }

  private void handleSQLException(SQLException e) {
    String sqlState = e.getSQLState();
    if (sqlState != null) {
      if (sqlState.startsWith("08")) { // Connection Exception
        throw new ModuleException("Database connection failed: " + e.getMessage(), MuleVectorsErrorType.CONNECTION_FAILED, e);
      } else if (sqlState.equals("28P01")) { // Invalid Password
        throw new ModuleException("Database authentication failed: " + e.getMessage(), MuleVectorsErrorType.AUTHENTICATION, e);
      }
    }
    throw new ModuleException("A database error occurred: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
  }

}
