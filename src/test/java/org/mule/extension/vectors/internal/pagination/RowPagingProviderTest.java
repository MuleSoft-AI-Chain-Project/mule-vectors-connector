package org.mule.extension.vectors.internal.pagination;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.extension.vectors.internal.service.store.VectorStoreService;
import org.mule.extension.vectors.internal.service.store.VectorStoreServiceProviderFactory;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RowPagingProviderTest {

  @Mock
  StoreConfiguration storeConfiguration;
  @Mock
  BaseStoreConnection storeConnection;
  @Mock
  QueryParameters queryParameters;
  @Mock
  StreamingHelper streamingHelper;
  @Mock
  VectorStoreService vectorStoreService;
  @Mock
  VectoreStoreIterator<?> rowIterator;

  RowPagingProvider pagingProvider;

  @BeforeEach
  void setUp() {
    pagingProvider = new RowPagingProvider(storeConfiguration, "testStore", queryParameters, streamingHelper);
  }

  @Test
  void getPage_whenNoMoreRows_returnsEmpty() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");
    doReturn(rowIterator).when(vectorStoreService).getFileIterator();
    when(rowIterator.hasNext()).thenReturn(false);

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenReturn(vectorStoreService);
      var result = pagingProvider.getPage(storeConnection);
      assertThat(result).isEmpty();
    }
  }

  @Test
  void getPage_whenUnsupportedOperation_throwsModuleException() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("UNSUPPORTED");

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenThrow(new ModuleException("Not supported",
                                         org.mule.extension.vectors.internal.error.MuleVectorsErrorType.STORE_SERVICES_FAILURE));
      assertThatThrownBy(() -> pagingProvider.getPage(storeConnection))
          .isInstanceOf(ModuleException.class);
    }
  }

  @Test
  void getPage_whenUnsupportedOperationException_throwsModuleException() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");
    doReturn(rowIterator).when(vectorStoreService).getFileIterator();
    when(rowIterator.hasNext()).thenThrow(new UnsupportedOperationException("not supported"));

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenReturn(vectorStoreService);
      assertThatThrownBy(() -> pagingProvider.getPage(storeConnection))
          .isInstanceOf(ModuleException.class);
    }
  }

  @Test
  void getPage_whenInterruptedException_throwsModuleException() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenThrow(new InterruptedException("interrupted"));
      assertThatThrownBy(() -> pagingProvider.getPage(storeConnection))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Error while getting row from testStore");
      Thread.interrupted();
    }
  }

  @Test
  void getTotalResults_returnsEmpty() {
    Optional<Integer> total = pagingProvider.getTotalResults(storeConnection);
    assertThat(total).isEmpty();
  }

  @Test
  void useStickyConnections_returnsTrue() {
    assertThat(pagingProvider.useStickyConnections()).isTrue();
  }

  @Test
  void close_doesNotThrow() {
    assertThatCode(() -> pagingProvider.close(storeConnection)).doesNotThrowAnyException();
  }

  @Test
  void getPage_withRows_returnsResults() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");
    doReturn(rowIterator).when(vectorStoreService).getFileIterator();
    when(rowIterator.hasNext()).thenReturn(true).thenReturn(false);

    VectorStoreRow<Object> row = mock(VectorStoreRow.class);
    doReturn(row).when(rowIterator).next();

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class);
        MockedStatic<JsonUtils> jsonUtils = Mockito.mockStatic(JsonUtils.class)) {

      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenReturn(vectorStoreService);

      JSONObject json = new JSONObject();
      json.put("embeddingId", "id1");
      jsonUtils.when(() -> JsonUtils.rowToJson(any())).thenReturn(json);

      when(streamingHelper.resolveCursorProvider(any())).thenReturn(mock(CursorProvider.class));

      var result = pagingProvider.getPage(storeConnection);
      assertThat(result).isNotEmpty();
    }
  }

  @Test
  void getPage_withNullRow_returnsEmptyList() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");
    doReturn(rowIterator).when(vectorStoreService).getFileIterator();
    when(rowIterator.hasNext()).thenReturn(true).thenReturn(false);
    doReturn(null).when(rowIterator).next();

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {

      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenReturn(vectorStoreService);

      var result = pagingProvider.getPage(storeConnection);
      assertThat(result).isEmpty();
    }
  }

  @Test
  void getPage_processNextRowException_returnsEmptyList() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");
    doReturn(rowIterator).when(vectorStoreService).getFileIterator();
    when(rowIterator.hasNext()).thenReturn(true).thenReturn(false);
    doThrow(new RuntimeException("fetch error")).when(rowIterator).next();

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {

      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenReturn(vectorStoreService);

      var result = pagingProvider.getPage(storeConnection);
      assertThat(result).isEmpty();
    }
  }

  @Test
  void getPage_withGeneralException_throwsModuleException() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {

      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenThrow(new RuntimeException("unexpected error"));

      assertThatThrownBy(() -> pagingProvider.getPage(storeConnection))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Error while getting row from testStore");
    }
  }

  @Test
  void getPage_secondCallReusesExistingIterator() throws Exception {
    when(storeConnection.getVectorStore()).thenReturn("MILVUS");
    doReturn(rowIterator).when(vectorStoreService).getFileIterator();
    when(rowIterator.hasNext()).thenReturn(false);

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {

      factory.when(() -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()))
          .thenReturn(vectorStoreService);

      pagingProvider.getPage(storeConnection);
      pagingProvider.getPage(storeConnection);

      factory.verify(
                     () -> VectorStoreServiceProviderFactory.getService(any(), any(), any(), any(), anyInt(), anyBoolean()),
                     times(1));
    }
  }
}
