package org.mule.extension.vectors.internal.store.pgvector;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.postgresql.ds.PGSimpleDataSource;
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

  @Override
  public Iterator<BaseStoreService.Row<?>> getRowIterator() {
    try {
      return new RowIterator();
    } catch (SQLException e) {
        handleSQLException(e);
        return null; // Should not be reached due to exception being thrown
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

  /**
   * Iterator to handle metadata pagination from the PostgreSQL database.
   */
  private class PgVectorMetadataIterator implements Iterator<ResultSet>, AutoCloseable {

    private int offset = 0; // Current offset for pagination
    private ResultSet resultSet;
    private PreparedStatement pstmt;
    private Connection connection;
    private String table;
    int pageSize;

    /**
     * Constructs a PgVectorMetadataIterator for fetching metadata from the database in pages.
     *
     * @param table The table to fetch metadata from.
     * @param pageSize The number of rows per page for pagination.
     * @throws SQLException If a database error occurs.
     */
    private PgVectorMetadataIterator(String table, int pageSize) throws SQLException {

      connection = dataSource.getConnection();

      this.table = table;
      this.pageSize = pageSize;

      fetchNextPage();
    }

    /**
     * Fetches the next page of metadata from the database.
     *
     * @throws SQLException If a database error occurs.
     */
    private void fetchNextPage() throws SQLException {
      if (pstmt != null) {
        pstmt.close();
      }

      String query = "SELECT " +
          ID_DEFAULT_FIELD_NAME + ", " +
          TEXT_DEFAULT_FIELD_NAME + ", " +
          (queryParams.retrieveEmbeddings() ? (VECTOR_DEFAULT_FIELD_NAME  + ", ") : "") +
          METADATA_DEFAULT_FIELD_NAME  +
          " FROM " + table + " LIMIT ? OFFSET ?";
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, this.pageSize);
      pstmt.setInt(2, offset);
      resultSet = pstmt.executeQuery();
      offset += pageSize;
    }

    /**
     * Checks if there are more elements in the result set.
     *
     * @return {@code true} if there are more elements, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
      try {
        // Check if the resultSet has more rows or fetch the next page
        if (resultSet != null && resultSet.next()) {
          return true;
        } else {
          fetchNextPage(); // Fetch the next page if the current page is exhausted
          return resultSet != null && resultSet.next();
        }
      } catch (SQLException e) {
        handleSQLException(e);
        return false; // Should not be reached
      }
    }

    /**
     * Returns the next metadata element in the result set.
     *
     * @return The next metadata element as a {@code String}.
     * @throws NoSuchElementException If no more elements are available.
     */
    @Override
    public ResultSet next() {
      if (resultSet == null) {
        throw new NoSuchElementException("No more elements available");
      }
      return resultSet;
    }

    /**
     * Closes the iterator and releases the resources.
     */
    public void close() {
      try {
        if (resultSet != null)
          resultSet.close();
        if (pstmt != null)
          pstmt.close();
        if (connection != null)
          connection.close();
      } catch (SQLException e) {
        LOGGER.error("Error closing database resources", e);
      }
    }
  }

  public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

    private final PgVectorMetadataIterator iterator;

    public RowIterator() throws SQLException {
      this.iterator = new PgVectorMetadataIterator(storeName, (int) queryParams.pageSize());
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public BaseStoreService.Row<?> next() {
      try {

        ResultSet resultSet = iterator.next();
        if (resultSet == null) {
          throw new NoSuchElementException("No more elements available");
        }

        String embeddingId = resultSet.getString(ID_DEFAULT_FIELD_NAME);
        float[] vector = null;
        if(queryParams.retrieveEmbeddings()) {
          String vectorString = resultSet.getString(VECTOR_DEFAULT_FIELD_NAME);
          String[] vectorStringArray =
              vectorString.replace("{", "").replace("}", "").replace("[", "").replace("]", "").split(",");
          vector = new float[vectorStringArray.length];
          for (int i = 0; i < vectorStringArray.length; i++) {
            vector[i] = Float.parseFloat(vectorStringArray[i].trim());
          }
        }
        String text = resultSet.getString(TEXT_DEFAULT_FIELD_NAME);
        JSONObject metadataObject = new JSONObject(resultSet.getString(METADATA_DEFAULT_FIELD_NAME));

        return new BaseStoreService.Row<TextSegment>(embeddingId,
                                    vector != null ? new Embedding(vector) : null,
                                    new TextSegment(text, Metadata.from(metadataObject.toMap())));

      } catch (SQLException e) {
        handleSQLException(e);
        throw new NoSuchElementException("Error processing next row");
      }
    }
  }
}
