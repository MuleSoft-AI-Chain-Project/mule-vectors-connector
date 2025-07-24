package org.mule.extension.vectors.internal.connection.provider.embeddings.openai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OpenAIModelConnectionTest {

  HttpClient httpClient;
  OpenAIModelConnection conn;

  @BeforeEach
  void setup() {
    httpClient = mock(HttpClient.class);
    conn = new OpenAIModelConnection("sk-abc", 1234L, httpClient);
  }

  @Test
  void constructorAndGetters() {
    assertEquals("sk-abc", conn.getApiKey());
    assertEquals(1234L, conn.getTimeout());
    assertEquals(httpClient, conn.getHttpClient());
    assertEquals(Constants.EMBEDDING_MODEL_SERVICE_OPENAI, conn.getEmbeddingModelService());
  }

  @Test
  void disconnectIsNoOp() {
    assertDoesNotThrow(() -> conn.disconnect());
  }

  @Test
  void validateSuccess() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    when(response.getStatusCode()).thenReturn(200);
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
    try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
      mocked.when(() -> HttpRequestHelper.executeGetRequest(any(), any(), any(), anyInt())).thenReturn(future);
      assertDoesNotThrow(() -> conn.validate());
    }
  }

  @Test
  void validate401ThrowsModuleException() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    when(response.getStatusCode()).thenReturn(401);
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
    try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
      mocked.when(() -> HttpRequestHelper.executeGetRequest(any(), any(), any(), anyInt())).thenReturn(future);
      ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
      assertEquals("Failed to validate connection to OpenAI.", ex.getMessage());
    }
  }

  @Test
  void validate403ThrowsModuleException() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    when(response.getStatusCode()).thenReturn(403);
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
    try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
      mocked.when(() -> HttpRequestHelper.executeGetRequest(any(), any(), any(), anyInt())).thenReturn(future);
      ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
      assertEquals("Failed to validate connection to OpenAI.", ex.getMessage());
    }
  }

  @Test
  void validate500ThrowsModuleException() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    when(response.getStatusCode()).thenReturn(500);
    org.mule.runtime.http.api.domain.entity.HttpEntity entity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
    when(entity.getBytes()).thenReturn("fail".getBytes());
    when(response.getEntity()).thenReturn(entity);
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
    try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
      mocked.when(() -> HttpRequestHelper.executeGetRequest(any(), any(), any(), anyInt())).thenReturn(future);
      ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
      assertEquals("Failed to validate connection to OpenAI.", ex.getMessage());
    }
  }
}
