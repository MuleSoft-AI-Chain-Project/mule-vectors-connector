package org.mule.extension.vectors.internal.service.store.pgvector;

import org.mule.extension.vectors.internal.connection.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
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

public class PGVectorStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PGVectorStoreIterator.class);

  private final DataSource dataSource;
  private final QueryParameters queryParams;
  private final int pageSize;
  private final PGVectorStoreConnection pgVectorStoreConnection;

  private PgVectorMetadataIterator iterator;

  public PGVectorStoreIterator(
      PGVectorStoreConnection pgVectorStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) {
    this.pgVectorStoreConnection = pgVectorStoreConnection;
    this.dataSource = pgVectorStoreConnection.getDataSource();
    this.queryParams = queryParams;
    this.pageSize = queryParams.pageSize();
    try {
      this.iterator = new PgVectorMetadataIterator(storeName, pageSize);
    } catch (SQLException e) {
      throw new ModuleException("Store issue",MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  private class PgVectorMetadataIterator implements Iterator<ResultSet>, AutoCloseable {

    private int offset = 0;
    private ResultSet resultSet;
    private PreparedStatement pstmt;
    private Connection connection;
    private String table;
    int pageSize;

    private PgVectorMetadataIterator(String table, int pageSize) throws SQLException {
      connection = dataSource.getConnection();
      this.table = table;
      this.pageSize = pageSize;
      fetchNextPage();
    }

    private void fetchNextPage() throws SQLException {
      if (pstmt != null) {
        pstmt.close();
      }

      String ID_DEFAULT_FIELD_NAME = "embedding_id";
      String TEXT_DEFAULT_FIELD_NAME = "text";
      String METADATA_DEFAULT_FIELD_NAME = "metadata";
      String VECTOR_DEFAULT_FIELD_NAME = "embedding";

      String query = "SELECT " +
          ID_DEFAULT_FIELD_NAME + ", " +
          TEXT_DEFAULT_FIELD_NAME + ", " +
          (queryParams.retrieveEmbeddings() ? (VECTOR_DEFAULT_FIELD_NAME + ", ") : "") +
          METADATA_DEFAULT_FIELD_NAME +
          " FROM " + table + " LIMIT ? OFFSET ?";
      pstmt = connection.prepareStatement(query);
      pstmt.setInt(1, this.pageSize);
      pstmt.setInt(2, offset);
      resultSet = pstmt.executeQuery();
      offset += pageSize;
    }

    @Override
    public boolean hasNext() {
      try {
        if (resultSet != null && resultSet.next()) {
          return true;
        } else {
          fetchNextPage();
          return resultSet != null && resultSet.next();
        }
      } catch (SQLException e) {
        handleSQLException(e);
        return false;
      }
    }

    @Override
    public ResultSet next() {
      try {
        if (resultSet == null || resultSet.isAfterLast()) {
          throw new NoSuchElementException();
        }
        return resultSet;
      } catch (SQLException e) {
        throw new NoSuchElementException();
      }
    }

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

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    try {
      ResultSet resultSet = iterator.next();
      if (resultSet == null || resultSet.isAfterLast()) {
        throw new NoSuchElementException();
      }

      String ID_DEFAULT_FIELD_NAME = "embedding_id";
      String TEXT_DEFAULT_FIELD_NAME = "text";
      String METADATA_DEFAULT_FIELD_NAME = "metadata";
      String VECTOR_DEFAULT_FIELD_NAME = "embedding";

      String embeddingId = resultSet.getString(ID_DEFAULT_FIELD_NAME);
      float[] vector = null;
      if (queryParams.retrieveEmbeddings()) {
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

      // This is the only place you may want to adapt for Embedded type.
      // If you want to keep it generic, you can cast or use a factory.
      // For now, we keep it as TextSegment to match the original.
      @SuppressWarnings("unchecked")
      Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataObject.toMap()));

      return new VectorStoreRow<>(embeddingId,
                                  vector != null ? new Embedding(vector) : null,
                                  embedded);

    } catch (NullPointerException e) {
      throw new NoSuchElementException();
    } catch (SQLException e) {
      handleSQLException(e);
      throw new NoSuchElementException("Error processing next row");
    }
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
