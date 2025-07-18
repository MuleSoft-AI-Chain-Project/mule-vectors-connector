package org.mule.extension.vectors.internal.service.store.alloydb;

import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class AlloyDBStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlloyDBStoreIterator.class);

  private final AlloyDBStoreConnection alloyDBStoreConnection;
  private final QueryParameters queryParams;
  private final int pageSize;

  private PgVectorMetadataIterator iterator;

  // Constructor: pass all required fields
  public AlloyDBStoreIterator(
      AlloyDBStoreConnection alloyDBStoreConnection,
      String storeName,
      QueryParameters queryParams
  )  {
    this.alloyDBStoreConnection = alloyDBStoreConnection;
    this.queryParams = queryParams;
    this.pageSize = queryParams.pageSize();
    try {
      this.iterator = new PgVectorMetadataIterator(storeName, pageSize);
    } catch (SQLException e) {
     throw new ModuleException("Authentication failed: " , MuleVectorsErrorType.AUTHENTICATION, e);
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

    private PgVectorMetadataIterator(String table, int pageSize) throws SQLException {
      connection = alloyDBStoreConnection.getAlloyDBEngine().getConnection();
      this.table = table;
      this.pageSize = pageSize;
      fetchNextPage();
    }

    private void fetchNextPage() throws SQLException {
      if (pstmt != null) {
        pstmt.close();
      }

      String idDefaultFieldName = "langchain_id";
      String textDefaultFieldName = "content";
      String metadataDefaultFieldName = "langchain_metadata";
      String vectorDefaultFieldName = "embedding";

      String query = "SELECT " +
          idDefaultFieldName + ", " +
          textDefaultFieldName + ", " +
          (queryParams.retrieveEmbeddings() ? (vectorDefaultFieldName + ", ") : "") +
          metadataDefaultFieldName +
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
        if (resultSet == null || !resultSet.next()) {
          throw new NoSuchElementException("No more elements available");
        }
      } catch (SQLException e) {
        handleSQLException(e);
        throw new NoSuchElementException("Error processing next row");
      }
      return resultSet;
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
      if (resultSet == null) {
        throw new NoSuchElementException("No more elements available");
      }

      String idDefaultFieldName = "langchain_id";
      String textDefaultFieldName = "content";
      String metadataDefaultFieldName = "langchain_metadata";
      String vectorDefaultFieldName = "embedding";

      String embeddingId = resultSet.getString(idDefaultFieldName);
      float[] vector = null;
      if (queryParams.retrieveEmbeddings()) {
        String vectorString = resultSet.getString(vectorDefaultFieldName);
        String[] vectorStringArray =
            vectorString.replace("{", "").replace("}", "").replace("[", "").replace("]", "").split(",");
        vector = new float[vectorStringArray.length];
        for (int i = 0; i < vectorStringArray.length; i++) {
          vector[i] = Float.parseFloat(vectorStringArray[i].trim());
        }
      }
      String text = resultSet.getString(textDefaultFieldName);
      JSONObject metadataObject = new JSONObject(resultSet.getString(metadataDefaultFieldName));

      // This is the only place you may want to adapt for Embedded type.
      // If you want to keep it generic, you can cast or use a factory.
      // For now, we keep it as TextSegment to match the original.
      @SuppressWarnings("unchecked")
      Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataObject.toMap()));

      return new VectorStoreRow<>(embeddingId,
                                  vector != null ? new Embedding(vector) : null,
                                  embedded);

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
