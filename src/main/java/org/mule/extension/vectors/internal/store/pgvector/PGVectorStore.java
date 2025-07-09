package org.mule.extension.vectors.internal.store.pgvector;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a store for vector data using PostgreSQL with PGVector extension.
 * This class is responsible for interacting with a PostgreSQL database to store and retrieve vector metadata.
 */
public class PGVectorStore extends BaseStoreService {

  static final String ID_DEFAULT_FIELD_NAME = "embedding_id";
  static final String TEXT_DEFAULT_FIELD_NAME = "text";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "embedding";

  private static final Logger LOGGER = LoggerFactory.getLogger(PGVectorStore.class);

  private final DataSource dataSource;
  private final QueryParameters queryParams;

  /**
   * Constructs a PGVectorVectorStore instance using configuration and query parameters.
   *
   * @param storeConfiguration The configuration for connecting to the store.
   * @param pgVectorStoreConnection The connection to the PGVector store.
   * @param storeName          The name of the store.
   * @param queryParams        Parameters related to query configurations.
   * @param dimension          The dimension of the vectors.
   * @param createStore        Whether to create the store if it does not exist.
   */
  public PGVectorStore(StoreConfiguration storeConfiguration, PGVectorStoreConnection pgVectorStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    super(storeConfiguration, pgVectorStoreConnection, storeName, dimension, createStore);
    this.dataSource = pgVectorStoreConnection.getDataSource();
    this.queryParams = queryParams;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      return PgVectorEmbeddingStore.datasourceBuilder()
              .datasource(this.dataSource)
              .table(this.storeName)
              .dimension(this.dimension)
              .createTable(this.createStore)
              .build();
    } catch (Exception e) {
        if (e.getCause() instanceof SQLException) {
            handleSQLException((SQLException) e.getCause());
        }
        throw new ModuleException("Failed to build PGVector embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }
  private void handleSQLException(SQLException e) {
    LOGGER.error("SQL error", e);
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
