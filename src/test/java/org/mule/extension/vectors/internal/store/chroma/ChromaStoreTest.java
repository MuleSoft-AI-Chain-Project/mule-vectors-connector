package org.mule.extension.vectors.internal.store.chroma;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.store.chroma.ChromaStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChromaStoreTest {

  @Mock
  StoreConfiguration storeConfiguration;
  @Mock
  ChromaStoreConnection chromaStoreConnection;
  @Mock
  QueryParameters queryParameters;
  @Mock
  HttpClient httpClient;
  @Mock
  HttpResponse httpResponse;

  ChromaStore chromaStore;

  @BeforeEach
  void setUp() {
    chromaStore = new ChromaStore(storeConfiguration, chromaStoreConnection, "testStore", queryParameters, 128, true);
  }

  @Test
  void constructor_assignsFields() throws Exception {
    assertThat(chromaStore).isNotNull();
    // Use reflection for private fields
    var queryParamsField = chromaStore.getClass().getDeclaredField("queryParams");
    queryParamsField.setAccessible(true);
    assertThat(queryParamsField.get(chromaStore)).isEqualTo(queryParameters);
    var connField = chromaStore.getClass().getDeclaredField("chromaStoreConnection");
    connField.setAccessible(true);
    assertThat(connField.get(chromaStore)).isEqualTo(chromaStoreConnection);
  }

  @Test
  void buildEmbeddingStore_returnsChromaEmbeddingStore() {
    try (MockedStatic<ChromaEmbeddingStore> staticMock = mockStatic(ChromaEmbeddingStore.class)) {
      ChromaEmbeddingStore.Builder builder = mock(ChromaEmbeddingStore.Builder.class, RETURNS_SELF);
      ChromaEmbeddingStore store = mock(ChromaEmbeddingStore.class);
      staticMock.when(ChromaEmbeddingStore::builder).thenReturn(builder);
      when(builder.baseUrl(any())).thenReturn(builder);
      when(builder.collectionName(any())).thenReturn(builder);
      when(builder.build()).thenReturn(store);
      EmbeddingStore<?> result = chromaStore.buildEmbeddingStore();
      assertThat(result).isSameAs(store);
      verify(builder).baseUrl(any());
      verify(builder).collectionName(any());
      verify(builder).build();
    }
  }

}
