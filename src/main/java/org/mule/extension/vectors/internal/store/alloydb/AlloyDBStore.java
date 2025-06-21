package org.mule.extension.vectors.internal.store.alloydb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.extension.vectors.internal.store.BaseStoreService.Row;
import org.postgresql.ds.PGSimpleDataSource;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEmbeddingStore;
import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import dev.langchain4j.community.store.embedding.alloydb.EmbeddingStoreConfig;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

public class AlloyDBStore extends BaseStoreService {
  
  static final String ID_DEFAULT_FIELD_NAME = "langchain_id";
  static final String TEXT_DEFAULT_FIELD_NAME = "content";
  static final String METADATA_DEFAULT_FIELD_NAME = "langchain_metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "embedding";

  private final AlloyDBEngine alloyDBEngine;
  private final QueryParameters queryParams;

  public AlloyDBStore(StoreConfiguration compositeConfiguration, AlloyDBStoreConnection alloyDBStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    
    super(compositeConfiguration, alloyDBStoreConnection, storeName, dimension, createStore);
    this.alloyDBEngine = alloyDBStoreConnection.getAlloyDBEngine();
    this.queryParams = queryParams;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    if(createStore) {
      
      EmbeddingStoreConfig embeddingStoreConfig = EmbeddingStoreConfig
      .builder(storeName, dimension)
      .overwriteExisting(false)
      .build();

      this.alloyDBEngine.initVectorStoreTable(embeddingStoreConfig);
    } 

    return new AlloyDBEmbeddingStore.Builder(this.alloyDBEngine, storeName)
        .build();
  }

  @Override
  public Iterator<BaseStoreService.Row<?>> getRowIterator() {
    try {
      return new RowIterator();
    } catch (SQLException e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
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

      connection = alloyDBEngine.getConnection();
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
        LOGGER.error("Error checking for next element", e);
        return false;
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
        LOGGER.error("Error closing resources", e);
      }
    }
  }

  public class RowIterator implements Iterator<Row<?>> {

    private final PgVectorMetadataIterator iterator;

    public RowIterator() throws SQLException {
      this.iterator = new PgVectorMetadataIterator(storeName, (int) queryParams.pageSize());
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Row<?> next() {
      try {

        ResultSet resultSet = iterator.next();
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

        return new Row<TextSegment>(embeddingId,
                                    vector != null ? new Embedding(vector) : null,
                                    new TextSegment(text, Metadata.from(metadataObject.toMap())));

      } catch (SQLException e) {
        LOGGER.error("Error while fetching next row", e);
        return null;
      }
    }
  }
}
