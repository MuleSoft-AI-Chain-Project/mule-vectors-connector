package org.mule.extension.vectors.internal.service.store.pgvector;

import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.extension.vectors.internal.service.store.BaseDatabaseIterator;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class PGVectorStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {
  private final DataSource dataSource;
  private final QueryParameters queryParams;
  private final int pageSize;

  private PGVectorDatabaseIterator iterator;

  public PGVectorStoreIterator(
      PGVectorStoreConnection pgVectorStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) {
    this.dataSource = pgVectorStoreConnection.getDataSource();
    this.queryParams = queryParams;
    this.pageSize = queryParams.pageSize();
    try {
      this.iterator = new PGVectorDatabaseIterator(storeName, pageSize, queryParams);
    } catch (SQLException e) {
      throw new ModuleException("Store issue",MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  /**
   * PGVector-specific database iterator implementation.
   */
  private class PGVectorDatabaseIterator extends BaseDatabaseIterator {

    private PGVectorDatabaseIterator(String table, int pageSize, QueryParameters queryParams) throws SQLException {
      super(table, pageSize, queryParams);
    }

    @Override
    protected Connection getConnection() throws SQLException {
      return dataSource.getConnection();
    }

    @Override
    protected DatabaseFieldNames getFieldNames() {
      return new DatabaseFieldNames(
          "embedding_id",  // id field
          "text",          // text field
          "metadata",      // metadata field
          "embedding"      // vector field
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
      if (resultSet == null || resultSet.isAfterLast()) {
        throw new NoSuchElementException();
      }

      String idDefaultFieldName = "embedding_id";
      String textDefaultFieldName = "text";
      String metadataDefaultFieldName = "metadata";
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

    } catch (NullPointerException e) {
      throw new NoSuchElementException();
    } catch (SQLException e) {
      iterator.handleSQLException(e);
      throw new NoSuchElementException("Error processing next row");
    }
  }
}
