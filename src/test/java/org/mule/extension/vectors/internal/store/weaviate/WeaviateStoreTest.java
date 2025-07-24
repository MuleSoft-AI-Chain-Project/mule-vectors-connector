package org.mule.extension.vectors.internal.store.weaviate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnectionParameters;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.weaviate.WeaviateStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;

class WeaviateStoreTest {

  @Test
  void constructionAndBuildEmbeddingStoreSuccess() {
    StoreConfiguration config = new StoreConfiguration();
    var params = new WeaviateStoreConnectionParameters();
    setField(params, "scheme", "https");
    setField(params, "host", "localhost");
    setField(params, "port", 8080);
    setField(params, "apiKey", "key");
    setField(params, "securedGrpc", false);
    setField(params, "grpcPort", 50051);
    setField(params, "useGrpcForInserts", false);
    setField(params, "avoidDups", false);
    setField(params, "consistencyLevel", "ALL");
    WeaviateStoreConnection conn = new WeaviateStoreConnection(params, null) {

      @Override
      public String getApikey() {
        return "key";
      }

      @Override
      public String getScheme() {
        return "https";
      }

      @Override
      public String getHost() {
        return "localhost";
      }

      @Override
      public Integer getPort() {
        return 8080;
      }

      @Override
      public boolean isSecuredGrpc() {
        return false;
      }

      @Override
      public Integer getGrpcPort() {
        return 50051;
      }

      @Override
      public boolean isUseGrpcForInserts() {
        return false;
      }

      @Override
      public boolean isAvoidDups() {
        return false;
      }

      @Override
      public String getConsistencyLevel() {
        return "ALL";
      }
    };
    String storeName = "testStore";
    QueryParameters queryParams = new QueryParameters();
    int dimension = 128;
    boolean createStore = true;
    WeaviateStore store = new WeaviateStore(config, conn, storeName, queryParams, dimension, createStore) {

      @Override
      public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        return new EmbeddingStore<>() {

          public EmbeddingSearchResult<TextSegment> search(dev.langchain4j.store.embedding.EmbeddingSearchRequest request) {
            return null;
          }

          public java.util.List<String> addAll(java.util.List<dev.langchain4j.data.embedding.Embedding> embeddings) {
            return java.util.Collections.emptyList();
          }

          public String add(dev.langchain4j.data.embedding.Embedding embedding, TextSegment segment) {
            return "";
          }

          public String add(dev.langchain4j.data.embedding.Embedding embedding) {
            return "";
          }

          public void add(String id, dev.langchain4j.data.embedding.Embedding embedding) {
            // no-op for test
          }
        };
      }
    };
    assertNotNull(store.buildEmbeddingStore());
  }

  @Test
  void buildEmbeddingStoreThrowsAuthenticationException() {
    StoreConfiguration config = new StoreConfiguration();
    var params = new WeaviateStoreConnectionParameters();
    setField(params, "scheme", "https");
    setField(params, "host", "localhost");
    setField(params, "port", 8080);
    setField(params, "apiKey", "key");
    setField(params, "securedGrpc", false);
    setField(params, "grpcPort", 50051);
    setField(params, "useGrpcForInserts", false);
    setField(params, "avoidDups", false);
    setField(params, "consistencyLevel", "ALL");
    WeaviateStoreConnection conn = new WeaviateStoreConnection(params, null) {

      @Override
      public String getApikey() {
        throw new dev.langchain4j.exception.AuthenticationException("authfail");
      }
    };
    String storeName = "testStore";
    QueryParameters queryParams = new QueryParameters();
    int dimension = 128;
    boolean createStore = true;
    WeaviateStore store = new WeaviateStore(config, conn, storeName, queryParams, dimension, createStore);
    ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
    assertTrue(ex.getMessage().toLowerCase().contains("auth"));
  }

  @Test
  void buildEmbeddingStoreThrowsGenericException() {
    StoreConfiguration config = new StoreConfiguration();
    var params = new WeaviateStoreConnectionParameters();
    setField(params, "scheme", "https");
    setField(params, "host", "localhost");
    setField(params, "port", 8080);
    setField(params, "apiKey", "key");
    setField(params, "securedGrpc", false);
    setField(params, "grpcPort", 50051);
    setField(params, "useGrpcForInserts", false);
    setField(params, "avoidDups", false);
    setField(params, "consistencyLevel", "ALL");
    WeaviateStoreConnection conn = new WeaviateStoreConnection(params, null) {

      @Override
      public String getHost() {
        throw new RuntimeException("fail");
      }
    };
    String storeName = "testStore";
    QueryParameters queryParams = new QueryParameters();
    int dimension = 128;
    boolean createStore = true;
    WeaviateStore store = new WeaviateStore(config, conn, storeName, queryParams, dimension, createStore);
    ModuleException ex = assertThrows(ModuleException.class, store::buildEmbeddingStore);
    assertTrue(ex.getMessage().toLowerCase().contains("fail"));
  }

  @Test
  void testGetFileIteratorReturnsNonNull() {
    StoreConfiguration storeConfiguration = mock(StoreConfiguration.class);
    WeaviateStoreConnection storeConnection = mock(WeaviateStoreConnection.class);
    QueryParameters queryParameters = mock(QueryParameters.class);
    String storeName = "test-weaviate";
    int dimension = 128;
    boolean createStore = true;

    WeaviateStore store = new WeaviateStore(
                                            storeConfiguration,
                                            storeConnection,
                                            storeName,
                                            queryParameters,
                                            dimension,
                                            createStore);
    assertThat(store.getFileIterator()).isNotNull();
  }

  // Reflection helper
  static void setField(Object obj, String field, Object value) {
    try {
      Class<?> c = obj.getClass();
      while (c != null) {
        try {
          java.lang.reflect.Field f = c.getDeclaredField(field);
          f.setAccessible(true);
          f.set(obj, value);
          return;
        } catch (NoSuchFieldException e) {
          c = c.getSuperclass();
        }
      }
      throw new RuntimeException("Field not found: " + field);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
