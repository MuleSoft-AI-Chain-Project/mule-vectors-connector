package org.mule.extension.vectors.internal.service.store.alloydb;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import dev.langchain4j.community.store.embedding.alloydb.EmbeddingStoreConfig;
import dev.langchain4j.community.store.embedding.alloydb.MetadataColumn;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class CustomAlloyDBEngine extends AlloyDBEngine {

  public CustomAlloyDBEngine(Builder builder) {
    super(builder);
  }

  @Override
  public void initVectorStoreTable(EmbeddingStoreConfig embeddingStoreConfig) {
    try {
      try (
          Connection connection = this.getConnection();
          Statement statement = connection.createStatement();
      ) {
        statement.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        if (embeddingStoreConfig.getOverwriteExisting()) {
          statement.executeUpdate(String.format("DROP TABLE IF EXISTS \"%s\".\"%s\"", embeddingStoreConfig.getSchemaName(), embeddingStoreConfig.getTableName()));
        }

        String metadataClause = "";
        if (embeddingStoreConfig.getMetadataColumns() != null && !embeddingStoreConfig.getMetadataColumns().isEmpty()) {
          metadataClause = metadataClause + String.format(", %s", embeddingStoreConfig.getMetadataColumns().stream().map(
              MetadataColumn::generateColumnString).collect(Collectors.joining(", ")));
        }

        if (embeddingStoreConfig.getStoreMetadata()) {
          metadataClause = metadataClause + String.format(", %s", (new MetadataColumn(embeddingStoreConfig.getMetadataJsonColumn(), "JSON", true)).generateColumnString());
        }

        String createStatement = embeddingStoreConfig.getOverwriteExisting() ? "CREATE TABLE" : "CREATE TABLE IF NOT EXISTS";
        String query = String.format("%s \"%s\".\"%s\" (\"%s\" UUID PRIMARY KEY, \"%s\" TEXT NULL, \"%s\" vector(%d) NOT NULL%s)", createStatement, embeddingStoreConfig.getSchemaName(), embeddingStoreConfig.getTableName(), embeddingStoreConfig.getIdColumn(), embeddingStoreConfig.getContentColumn(), embeddingStoreConfig.getEmbeddingColumn(), embeddingStoreConfig.getVectorSize(), metadataClause);
        statement.executeUpdate(query);
      }

    } catch (SQLException ex) {
      throw new org.mule.runtime.extension.api.exception.ModuleException(
          String.format("Failed to initialize vector store table: \"%s\".\"%s\"", embeddingStoreConfig.getSchemaName(), embeddingStoreConfig.getTableName()),
          org.mule.extension.vectors.internal.error.MuleVectorsErrorType.STORE_CONNECTION_FAILURE,
          ex);
    }
  }

  public static class Builder extends AlloyDBEngine.Builder {

    @Override
    public CustomAlloyDBEngine build() {
      return new CustomAlloyDBEngine(this);
    }
  }
}
