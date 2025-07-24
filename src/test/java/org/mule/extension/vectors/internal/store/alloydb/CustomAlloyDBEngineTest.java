package org.mule.extension.vectors.internal.store.alloydb;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.service.store.alloydb.CustomAlloyDBEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import dev.langchain4j.community.store.embedding.alloydb.EmbeddingStoreConfig;
import dev.langchain4j.community.store.embedding.alloydb.MetadataColumn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CustomAlloyDBEngineTest {

  CustomAlloyDBEngine engine;
  Connection connection;
  Statement statement;

  @BeforeEach
  void setup() throws Exception {
    engine = mock(CustomAlloyDBEngine.class);
    connection = mock(Connection.class);
    statement = mock(Statement.class);
    when(engine.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    // Use doCallRealMethod for the method under test
    doCallRealMethod().when(engine).initVectorStoreTable(any(EmbeddingStoreConfig.class));
  }

  @Test
  void initVectorStoreTable_success_no_metadata() throws Exception {
    EmbeddingStoreConfig config = EmbeddingStoreConfig.builder("myschema.mytable", 128).overwriteExisting(false).build();
    engine.initVectorStoreTable(config);
    verify(statement).executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
    verify(statement, never()).executeUpdate(contains("DROP TABLE"));
    verify(statement).executeUpdate(contains("CREATE TABLE IF NOT EXISTS"));
  }

  @Test
  void initVectorStoreTable_success_with_overwrite() throws Exception {
    EmbeddingStoreConfig config = EmbeddingStoreConfig.builder("myschema.mytable", 128).overwriteExisting(true).build();
    engine.initVectorStoreTable(config);
    verify(statement).executeUpdate(contains("DROP TABLE IF EXISTS"));
    verify(statement).executeUpdate(contains("CREATE TABLE"));
  }

  @Test
  void initVectorStoreTable_success_with_metadata_columns() throws Exception {
    List<MetadataColumn> metaCols = List.of(new MetadataColumn("foo", "TEXT", false));
    EmbeddingStoreConfig config = EmbeddingStoreConfig.builder("myschema.mytable", 128)
        .overwriteExisting(false)
        .metadataColumns(metaCols)
        .build();
    engine.initVectorStoreTable(config);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(statement, times(2)).executeUpdate(captor.capture());
    assertThat(captor.getAllValues().stream().anyMatch(sql -> sql.contains("foo\" TEXT NOT NULL"))).isTrue();
  }

  @Test
  void initVectorStoreTable_success_with_store_metadata() throws Exception {
    EmbeddingStoreConfig config = EmbeddingStoreConfig.builder("myschema.mytable", 128)
        .overwriteExisting(false)
        .storeMetadata(true)
        .build();
    engine.initVectorStoreTable(config);
    verify(statement).executeUpdate(contains("JSON"));
  }

  @Test
  void initVectorStoreTable_SQLException_is_wrapped() throws Exception {
    when(connection.createStatement()).thenThrow(new SQLException("fail"));
    EmbeddingStoreConfig config = EmbeddingStoreConfig.builder("myschema.mytable", 128).overwriteExisting(false).build();
    Throwable thrown = catchThrowable(() -> engine.initVectorStoreTable(config));
    assertThat(thrown)
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to initialize vector store table");
    assertThat(thrown.getCause()).isInstanceOf(SQLException.class);
  }
}
