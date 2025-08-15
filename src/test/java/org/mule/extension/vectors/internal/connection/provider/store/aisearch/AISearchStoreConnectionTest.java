package org.mule.extension.vectors.internal.connection.provider.store.aisearch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AISearchStoreConnectionTest {

  AISearchStoreConnectionParameters params;
  HttpClient httpClient;
  AISearchStoreConnection conn;

  @BeforeEach
  void setUp() {
    params = mock(AISearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("https://test.search.windows.net");
    when(params.getApiKey()).thenReturn("secret");
    httpClient = mock(HttpClient.class);
    conn = new AISearchStoreConnection(params, httpClient);
  }

  @Test
  void getters_work() {
    assertEquals("https://test.search.windows.net", conn.getUrl());
    assertEquals("secret", conn.getApiKey());
    assertEquals(params, conn.getConnectionParameters());
    assertEquals(httpClient, conn.getHttpClient());
  }

  @Test
  void getVectorStore_returnsConstant() {
    assertEquals("AI_SEARCH", conn.getVectorStore());
  }

  @Test
  void disconnect_noop() {
    assertDoesNotThrow(() -> conn.disconnect());
  }

  @Test
  void buildHeaders_setsContentTypeAndApiKey() throws Exception {
    java.lang.reflect.Method m = AISearchStoreConnection.class.getDeclaredMethod("buildHeaders", String.class);
    m.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, String> headers = (Map<String, String>) m.invoke(conn, "application/json");
    assertEquals("application/json", headers.get("Content-Type"));
    assertEquals("secret", headers.get("api-key"));
  }

  @Test
  void validate_success() throws Exception {
    // Mock static HttpRequestHelper
    try (var helper = mockStatic(org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.class)) {
      HttpResponse response = mock(HttpResponse.class);
      when(response.getStatusCode()).thenReturn(200);
      when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("ok".getBytes()));
      helper.when(() -> org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.executeGetRequest(any(), anyString(),
                                                                                                               any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(response));
      conn.validate(); // should not throw
    }
  }

  @Test
  void validate_missingUrl() {
    when(params.getUrl()).thenReturn(null);
    AISearchStoreConnection conn = new AISearchStoreConnection(params, httpClient);
    ModuleException ex = assertThrows(ModuleException.class, conn::validate);
    assertTrue(ex.getMessage().contains("URL is required"));
  }

  @Test
  void validate_missingApiKey() {
    when(params.getApiKey()).thenReturn(null);
    AISearchStoreConnection conn = new AISearchStoreConnection(params, httpClient);
    ModuleException ex = assertThrows(ModuleException.class, conn::validate);
    assertTrue(ex.getMessage().contains("API Key is required"));
  }

  @Test
  void validate_httpError() {
    try (var helper = mockStatic(org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.class)) {
      HttpResponse response = mock(HttpResponse.class);
      when(response.getStatusCode()).thenReturn(401);
      when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("fail".getBytes()));
      helper.when(() -> org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.executeGetRequest(any(), anyString(),
                                                                                                               any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(response));
      ModuleException ex = assertThrows(ModuleException.class, conn::validate);
      String msg = ex.getMessage();
      assertTrue(msg.contains("Failed to connect to AI search"), "Actual: " + msg);
    }
  }

  @Test
  void validate_errorReadingBody() {
    try (var helper = mockStatic(org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.class)) {
      HttpResponse response = mock(HttpResponse.class);
      when(response.getStatusCode()).thenReturn(401);
      when(response.getEntity()).thenThrow(new RuntimeException("fail"));
      helper.when(() -> org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.executeGetRequest(any(), anyString(),
                                                                                                               any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(response));
      ModuleException ex = assertThrows(ModuleException.class, conn::validate);
      assertTrue(ex.getMessage().toLowerCase().contains("fail"));
    }
  }
}
