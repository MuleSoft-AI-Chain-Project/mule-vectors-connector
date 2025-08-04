package org.mule.extension.vectors.internal.store.aisearch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.aisearch.AISearchStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;

class AISearchStoreTest {

  @Test
  void constructionAndBuildEmbeddingStore_success() {
    StoreConfiguration config = new StoreConfiguration();
    AISearchStoreConnection conn = mock(AISearchStoreConnection.class);
    when(conn.getUrl()).thenReturn("http://localhost");
    when(conn.getApiKey()).thenReturn("key");
    QueryParameters queryParams = new QueryParameters();
    AISearchStore store = new AISearchStore(config, conn, "store", queryParams, 128, false);
    EmbeddingStore<TextSegment> embeddingStore = store.buildEmbeddingStore();
    assertNotNull(embeddingStore);
  }

  @Test
  void buildEmbeddingStore_throwsModuleException_onHttpResponseException401() {
    StoreConfiguration config = new StoreConfiguration();
    var params = new AISearchStoreConnectionParameters();
    AISearchStoreConnection conn = new AISearchStoreConnection(params, null) {

      @Override
      public String getApiKey() {
        return "key";
      }

      @Override
      public String getUrl() {
        return "endpoint";
      }
    };
    String storeName = "testStore";
    QueryParameters queryParams = new QueryParameters();
    int dimension = 128;
    boolean createStore = true;
    AISearchStore store = new AISearchStore(config, conn, storeName, queryParams, dimension, createStore) {

      @Override
      public EmbeddingStore<dev.langchain4j.data.segment.TextSegment> buildEmbeddingStore() {
        com.azure.core.exception.HttpResponseException ex = new com.azure.core.exception.HttpResponseException("401", null, null);
        throw new org.mule.runtime.extension.api.exception.ModuleException("fail", MuleVectorsErrorType.STORE_CONNECTION_FAILURE,
                                                                           ex);
      }
    };
    ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
    assertTrue(ex.getMessage().toLowerCase().contains("fail"));
  }

  @Test
  void buildEmbeddingStore_throwsModuleException_onGenericException() {
    StoreConfiguration config = new StoreConfiguration();
    AISearchStoreConnection conn = mock(AISearchStoreConnection.class);
    when(conn.getUrl()).thenReturn("http://localhost");
    when(conn.getApiKey()).thenReturn("key");
    QueryParameters queryParams = new QueryParameters();
    AISearchStore store = new AISearchStore(config, conn, "store", queryParams, 128, false) {

      @Override
      public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        throw new RuntimeException("Some error");
      }
    };
    assertThrows(RuntimeException.class, store::buildEmbeddingStore);
  }
}
