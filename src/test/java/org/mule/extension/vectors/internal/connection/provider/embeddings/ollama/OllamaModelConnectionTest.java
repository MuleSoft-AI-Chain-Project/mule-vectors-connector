package org.mule.extension.vectors.internal.connection.provider.embeddings.ollama;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OllamaModelConnectionTest {

  @Mock
  HttpClient httpClient;
  OllamaModelConnection conn;

  @BeforeEach
  void setup() {
    conn = new OllamaModelConnection("http://localhost:1234", 1000L, httpClient);
  }

  @Test
  void getters_work() {
    assertEquals("http://localhost:1234", conn.getBaseUrl());
    assertEquals(1000L, conn.getTimeout());
    assertEquals(httpClient, conn.getHttpClient());
  }

  @Test
  void getEmbeddingModelService_returnsConstant() {
    assertEquals(Constants.EMBEDDING_MODEL_SERVICE_OLLAMA, conn.getEmbeddingModelService());
  }

  @Test
  void disconnect_isNoOp() {
    assertDoesNotThrow(conn::disconnect);
  }

  @Test
  void validate_success() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    lenient().when(response.getStatusCode()).thenReturn(200);
    lenient().when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("ok".getBytes()));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
      assertDoesNotThrow(conn::validate);
    }
  }

  @Test
  void validate_non200_throwsModuleException() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    when(response.getStatusCode()).thenReturn(500);
    when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("fail".getBytes()));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
      ModuleException ex = assertThrows(ModuleException.class, conn::validate);
      assertTrue(ex.getMessage().contains("Failed to validate connection"));
    }
  }

  @Test
  void validate_interruptedOrExecutionException_throwsModuleException() throws Exception {
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("fail")));
      ModuleException ex = assertThrows(ModuleException.class, conn::validate);
      assertTrue(ex.getMessage().contains("Failed to validate connection"));
    }
  }
}
