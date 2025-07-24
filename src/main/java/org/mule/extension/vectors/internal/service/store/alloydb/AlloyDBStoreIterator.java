package org.mule.extension.vectors.internal.service.store.alloydb;

import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseDatabaseIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONObject;

public class AlloyDBStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private final AlloyDBStoreConnection alloyDBStoreConnection;
  private final QueryParameters queryParams;
  private final int pageSize;

  private AlloyDBDatabaseIterator iterator;

  // Constructor: pass all required fields
  public AlloyDBStoreIterator(
                              AlloyDBStoreConnection alloyDBStoreConnection,
                              String storeName,
                              QueryParameters queryParams) {
    this.alloyDBStoreConnection = alloyDBStoreConnection;
    this.queryParams = queryParams;
    this.pageSize = queryParams.pageSize();
    try {
      this.iterator = new AlloyDBDatabaseIterator(storeName, pageSize, queryParams);
    } catch (SQLException e) {
      throw new ModuleException("Authentication failed: ", MuleVectorsErrorType.AUTHENTICATION, e);
    }
  }

  /**
   * AlloyDB-specific database iterator implementation.
   */
  private class AlloyDBDatabaseIterator extends BaseDatabaseIterator {

    private AlloyDBDatabaseIterator(String table, int pageSize, QueryParameters queryParams) throws SQLException {
      super(table, pageSize, queryParams);
    }

    @Override
    protected Connection getConnection() throws SQLException {
      return alloyDBStoreConnection.getAlloyDBEngine().getConnection();
    }

    @Override
    protected DatabaseFieldNames getFieldNames() {
      return new DatabaseFieldNames(
                                    "langchain_id", // id field
                                    "content", // text field
                                    "langchain_metadata", // metadata field
                                    "embedding" // vector field
      );
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No more elements available");
    }

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
      iterator.handleSQLException(e);
      throw new NoSuchElementException("Error processing next row");
    }
  }
}
