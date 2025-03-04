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
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a store for vector data using PostgreSQL with PGVector extension.
 * This class is responsible for interacting with a PostgreSQL database to store and retrieve vector metadata.
 */
public class PGVectorStore extends BaseStore {

  static final String ID_DEFAULT_FIELD_NAME = "embedding_id";
  static final String TEXT_DEFAULT_FIELD_NAME = "text";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "embedding";

  private static final Logger LOGGER = LoggerFactory.getLogger(PGVectorStore.class);

  private String user;
  private String password;
  private String host;
  private int port;
  private String database;

  private DataSource dataSource;

  public javax.sql.DataSource getDataSource() {

    if(dataSource == null) {

      host = ValidationUtils.ensureNotBlank(host, "host");
      port = ValidationUtils.ensureGreaterThanZero(port, "port");
      user = ValidationUtils.ensureNotBlank(user, "user");
      password = ValidationUtils.ensureNotBlank(password, "password");
      database = ValidationUtils.ensureNotBlank(database, "database");
      PGSimpleDataSource source = new PGSimpleDataSource();
      source.setServerNames(new String[]{host});
      source.setPortNumbers(new int[]{port});
      source.setDatabaseName(database);
      source.setUser(user);
      source.setPassword(password);
      this.dataSource = source;
    }
    return dataSource;
  }

  /**
   * Constructs a PGVectorVectorStore instance using configuration and query parameters.
   *
   * @param storeName The name of the store.
   * @param storeConfiguration The configuration for connecting to the store.
   * @param queryParams Parameters related to query configurations.
   */
  public PGVectorStore(StoreConfiguration storeConfiguration, PGVectorStoreConnection pgVectorStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, pgVectorStoreConnection, storeName, queryParams, dimension, createStore);

    this.host = pgVectorStoreConnection.getHost();
    this.port = pgVectorStoreConnection.getPort();
    this.database = pgVectorStoreConnection.getDatabase();
    this.user = pgVectorStoreConnection.getUser();
    this.password = pgVectorStoreConnection.getPassword();
    this.dataSource = pgVectorStoreConnection.getDataSource();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    return PgVectorEmbeddingStore.datasourceBuilder()
        .datasource(getDataSource())
        .table(storeName)
        .dimension(dimension)
        .createTable(createStore)
        .build();
  }

  /**
   * Lists the sources stored in the PostgreSQL database.
   *
   * @return A {@link JSONObject} containing the sources and their metadata.
   */
  public JSONObject listSources() {

    HashMap<String, JSONObject> sourceObjectMap = new HashMap<>();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);

    try (PgVectorMetadataIterator iterator = new PgVectorMetadataIterator(user, password, host, port, database, storeName, (int)queryParams.embeddingPageSize())) {
      while (iterator.hasNext()) {

        ResultSet resultSet = iterator.next();
        JSONObject metadataObject = new JSONObject(resultSet.getString(Constants.STORE_SCHEMA_METADATA_FIELD_NAME));
        JSONObject sourceObject = getSourceObject(metadataObject);
        addOrUpdateSourceObjectIntoSourceObjectMap(sourceObjectMap, sourceObject);
      }
    } catch (SQLException e) {
      LOGGER.error("Error while listing sources", e);
    }

    jsonObject.put(Constants.JSON_KEY_SOURCES, JsonUtils.jsonObjectCollectionToJsonArray(sourceObjectMap.values()));
    jsonObject.put(Constants.JSON_KEY_SOURCE_COUNT, sourceObjectMap.size());

    return jsonObject;
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
     * @param userName The username for database access.
     * @param password The password for database access.
     * @param host The PostgreSQL host.
     * @param port The PostgreSQL port.
     * @param database The name of the database.
     * @param table The table to fetch metadata from.
     * @param pageSize The number of rows per page for pagination.
     * @throws SQLException If a database error occurs.
     */
    private PgVectorMetadataIterator(String userName, String password, String host, int port, String database, String table, int pageSize) throws SQLException {

      // Initialize the connection and the first page of data
      PGSimpleDataSource dataSource = new PGSimpleDataSource();
      dataSource.setServerNames(new String[]{host});
      dataSource.setPortNumbers(new int[]{port});
      dataSource.setDatabaseName(database);
      dataSource.setUser(userName);
      dataSource.setPassword(password);

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
          VECTOR_DEFAULT_FIELD_NAME  + ", " +
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

  @Override
  public RowIterator rowIterator() {
    try {
      return new RowIterator();
    } catch (SQLException e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {

    private PgVectorMetadataIterator iterator;

    public RowIterator() throws SQLException {

      super();
      this.iterator = new PgVectorMetadataIterator(
          user, password, host, port, database, storeName, (int)queryParams.embeddingPageSize());
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
        String vectorString = resultSet.getString(VECTOR_DEFAULT_FIELD_NAME);
        String[] vectorStringArray = vectorString.replace("{", "").replace("}", "").replace("[", "").replace("]", "").split(",");
        float[] vector = new float[vectorStringArray.length];
        for (int i = 0; i < vectorStringArray.length; i++) {
           vector[i] = Float.parseFloat(vectorStringArray[i].trim());
        }
        String text = resultSet.getString(TEXT_DEFAULT_FIELD_NAME);
        JSONObject metadataObject = new JSONObject(resultSet.getString(METADATA_DEFAULT_FIELD_NAME));

        return new Row<TextSegment>(embeddingId, new Embedding(vector), new TextSegment(text, Metadata.from(metadataObject.toMap())));

      } catch (SQLException e) {
        LOGGER.error("Error while fetching next row", e);
        return null;
      }
    }
  }
}
