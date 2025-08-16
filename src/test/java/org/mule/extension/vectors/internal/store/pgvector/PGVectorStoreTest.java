package org.mule.extension.vectors.internal.store.pgvector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.pgvector.PGVectorStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.sql.SQLException;

import javax.sql.DataSource;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PGVectorStoreTest {

  @Mock
  StoreConfiguration storeConfiguration;
  @Mock
  PGVectorStoreConnection pgVectorStoreConnection;
  @Mock
  DataSource dataSource;
  @Mock
  QueryParameters queryParameters;
  @Mock
  PgVectorEmbeddingStore embeddingStore;
  @Mock
  private PgVectorEmbeddingStore.DatasourceBuilder builder;

  @BeforeEach
  void setUp() {
    lenient().when(pgVectorStoreConnection.getDataSource()).thenReturn(dataSource);
  }

  @Test
  void testBuildEmbeddingStoreReturnsNonNull() {
    try (MockedStatic<PgVectorEmbeddingStore> staticMock = mockStatic(PgVectorEmbeddingStore.class)) {
      staticMock.when(PgVectorEmbeddingStore::datasourceBuilder).thenReturn(builder);
      when(builder.datasource(any())).thenReturn(builder);
      when(builder.table(any())).thenReturn(builder);
      when(builder.dimension(anyInt())).thenReturn(builder);
      when(builder.createTable(anyBoolean())).thenReturn(builder);
      when(builder.build()).thenReturn(embeddingStore);

      PGVectorStore store = new PGVectorStore(
                                              storeConfiguration,
                                              pgVectorStoreConnection,
                                              "testStore",
                                              queryParameters,
                                              3,
                                              true);
      EmbeddingStore<TextSegment> result = store.buildEmbeddingStore();
      assertNotNull(result);
    }
  }

  @Test
  void testBuildEmbeddingStoreThrowsModuleExceptionOnGenericException() {
    try (MockedStatic<PgVectorEmbeddingStore> staticMock = mockStatic(PgVectorEmbeddingStore.class)) {
      staticMock.when(PgVectorEmbeddingStore::datasourceBuilder).thenThrow(new RuntimeException("fail"));
      PGVectorStore store = new PGVectorStore(
                                              storeConfiguration,
                                              pgVectorStoreConnection,
                                              "testStore",
                                              queryParameters,
                                              3,
                                              true);
      ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
      assertTrue(ex.getMessage().contains("fail"));
    }
  }

  @Test
  void testBuildEmbeddingStoreThrowsModuleExceptionOnConnectionSQLException() {
    try (MockedStatic<PgVectorEmbeddingStore> staticMock = mockStatic(PgVectorEmbeddingStore.class)) {
      SQLException sqlEx = new SQLException("connection fail", "08001");
      staticMock.when(PgVectorEmbeddingStore::datasourceBuilder).thenThrow(new RuntimeException("fail", sqlEx));
      PGVectorStore store = new PGVectorStore(
                                              storeConfiguration,
                                              pgVectorStoreConnection,
                                              "testStore",
                                              queryParameters,
                                              3,
                                              true);
      ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
      assertTrue(ex.getMessage().contains("Database connection failed"));
    }
  }
}
