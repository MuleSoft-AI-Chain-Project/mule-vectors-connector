package org.mule.extension.vectors.internal.connection.provider.store.pgvector;

import static org.junit.jupiter.api.Assertions.*;
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
  private StoreConfiguration storeConfiguration;
  @Mock
  private PGVectorStoreConnection pgVectorStoreConnection;
  @Mock
  private QueryParameters queryParameters;
  @Mock
  private DataSource dataSource;
  @Mock
  private PgVectorEmbeddingStore.DatasourceBuilder builder;
  @Mock
  private PgVectorEmbeddingStore embeddingStore;

  private static final String STORE_NAME = "test-pgvector";
  private static final int DIMENSION = 128;
  private static final boolean CREATE_STORE = true;

  @BeforeEach
  void setUp() {
    when(pgVectorStoreConnection.getDataSource()).thenReturn(dataSource);
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
                                              STORE_NAME,
                                              queryParameters,
                                              DIMENSION,
                                              CREATE_STORE);
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
                                              STORE_NAME,
                                              queryParameters,
                                              DIMENSION,
                                              CREATE_STORE);
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
                                              STORE_NAME,
                                              queryParameters,
                                              DIMENSION,
                                              CREATE_STORE);
      ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
      assertTrue(ex.getMessage().contains("Database connection failed"));
    }
  }

  @Test
  void testBuildEmbeddingStoreThrowsModuleExceptionOnAuthSQLException() {
    try (MockedStatic<PgVectorEmbeddingStore> staticMock = mockStatic(PgVectorEmbeddingStore.class)) {
      SQLException sqlEx = new SQLException("auth fail", "28P01");
      staticMock.when(PgVectorEmbeddingStore::datasourceBuilder).thenThrow(new RuntimeException("fail", sqlEx));
      PGVectorStore store = new PGVectorStore(
                                              storeConfiguration,
                                              pgVectorStoreConnection,
                                              STORE_NAME,
                                              queryParameters,
                                              DIMENSION,
                                              CREATE_STORE);
      ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
      assertTrue(ex.getMessage().contains("Database authentication failed"));
    }
  }

  @Test
  void testBuildEmbeddingStoreThrowsModuleExceptionOnGenericSQLException() {
    try (MockedStatic<PgVectorEmbeddingStore> staticMock = mockStatic(PgVectorEmbeddingStore.class)) {
      SQLException sqlEx = new SQLException("other fail", "99999");
      staticMock.when(PgVectorEmbeddingStore::datasourceBuilder).thenThrow(new RuntimeException("fail", sqlEx));
      PGVectorStore store = new PGVectorStore(
                                              storeConfiguration,
                                              pgVectorStoreConnection,
                                              STORE_NAME,
                                              queryParameters,
                                              DIMENSION,
                                              CREATE_STORE);
      ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
      assertTrue(ex.getMessage().contains("A database error occurred"));
    }
  }
}
