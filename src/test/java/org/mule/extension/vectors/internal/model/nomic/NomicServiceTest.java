package org.mule.extension.vectors.internal.model.nomic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.nomic.NomicService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NomicServiceTest {

  @Mock
  NomicModelConnection modelConnection;
  @Mock
  EmbeddingModelParameters modelParameters;
  NomicService service;

  @BeforeEach
  void setUp() {
    service = new NomicService(modelConnection, modelParameters);
  }

  @Test
  void embedTexts_returnsEmbeddings() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
    String embeddingJson = "{" +
        "\"embeddings\": [[0.1, 0.2, 0.3]]," +
        "\"usage\": {\"total_tokens\": 42}" +
        "}";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      when(modelConnection.getHttpClient()).thenReturn(null);
      when(modelConnection.getApiKey()).thenReturn("key");
      when(modelConnection.getTimeout()).thenReturn(1000L);
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(embeddingJson);
      List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
      Response<List<Embedding>> resp = service.embedTexts(List.of("foo"));
      assertNotNull(resp);
      assertEquals(1, resp.content().size());
      float[] expected = new float[] {0.1f, 0.2f, 0.3f};
      assertArrayEquals(expected, resp.content().get(0).vector(), 1e-6f);
    }
  }

  @Test
  void embedTexts_handlesErrorResponse() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
    String errorJson = "{\"error\":\"fail\"}";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      when(modelConnection.getHttpClient()).thenReturn(null);
      when(modelConnection.getApiKey()).thenReturn("key");
      when(modelConnection.getTimeout()).thenReturn(1000L);
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenThrow(new ModuleException("Nomic API error (HTTP 500): " + errorJson,
                                         org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_FAILURE));
      List<String> texts = List.of("foo");
      assertThrows(ModuleException.class, () -> service.embedTexts(texts));
    }
  }

  @Test
  void embedTexts_handlesNullOrEmptyInput() {
    List<String> emptyList = Collections.emptyList();
    assertThrows(NullPointerException.class, () -> service.embedTexts(null));
    assertThrows(NullPointerException.class, () -> service.embedTexts(emptyList));
  }

  @Test
  void generateTextEmbeddings_normal() throws Exception {
    when(modelConnection.getHttpClient()).thenReturn(null);
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    String embeddingJson = "{" +
        "\"embeddings\": [[0.1, 0.2, 0.3]]," +
        "\"usage\": {\"total_tokens\": 42}" +
        "}";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(embeddingJson);
      Object result = service.generateTextEmbeddings(List.of("foo"), "test-model");
      assertTrue(result instanceof String);
      JSONObject json = new JSONObject((String) result);
      JSONArray arr = json.getJSONArray("embeddings");
      assertEquals(1, arr.length());
    }
  }

  @Test
  void generateTextEmbeddings_propagatesModuleException() throws Exception {
    when(modelConnection.getHttpClient()).thenReturn(null);
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenThrow(new ModuleException("fail",
                                         org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_FAILURE));
      // Extract the method call to avoid multiple invocations in lambda
      org.junit.jupiter.api.function.Executable methodCall =
          () -> service.generateTextEmbeddings(List.of("foo"), "test-model");

      assertThrows(ModuleException.class, methodCall);
    }
  }

  @Test
  void buildTextEmbeddingsPayload_serializesInput() throws Exception {
    var m = NomicService.class.getDeclaredMethod("buildTextEmbeddingsPayload", List.class, String.class);
    m.setAccessible(true);
    byte[] bytes = (byte[]) m.invoke(service, List.of("foo"), "bar");
    String json = new String(bytes, StandardCharsets.UTF_8);
    assertTrue(json.contains("foo"));
    assertTrue(json.contains("bar"));
  }

  @Test
  void buildAuthHeaders_containsAuthorization() throws Exception {
    when(modelConnection.getApiKey()).thenReturn("key");
    var m = NomicService.class.getDeclaredMethod("buildAuthHeaders");
    m.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, String> headers = (Map<String, String>) m.invoke(service);
    assertEquals("Bearer key", headers.get("Authorization"));
  }

  @Test
  void generateImageEmbeddings_nullImages_throwsIllegalArgument() {
    assertThrows(RuntimeException.class, () -> service.generateImageEmbeddings(null, "model"));
  }

  @Test
  void generateImageEmbeddings_emptyImages_throwsIllegalArgument() {
    assertThrows(RuntimeException.class, () -> service.generateImageEmbeddings(Collections.emptyList(), "model"));
  }

  @Test
  void generateImageEmbeddings_success() throws Exception {
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    String embeddingJson = "{\"embeddings\": [[0.4, 0.5]], \"usage\": {\"total_tokens\": 10}}";

    org.mule.runtime.http.api.client.HttpClient httpClient = mock(org.mule.runtime.http.api.client.HttpClient.class);
    when(modelConnection.getHttpClient()).thenReturn(httpClient);

    HttpResponse mockResp = mock(HttpResponse.class);
    when(mockResp.getStatusCode()).thenReturn(200);
    when(mockResp.getEntity()).thenReturn(
                                          new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(embeddingJson
                                              .getBytes(StandardCharsets.UTF_8)));
    when(httpClient.send(any(), any())).thenReturn(mockResp);

    Object result = service.generateImageEmbeddings(List.of(new byte[] {1, 2, 3}), "test-model");
    assertNotNull(result);
    assertTrue(((String) result).contains("embeddings"));
  }

  @Test
  void generateImageEmbeddings_non200_throwsRuntimeException() throws Exception {
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);

    org.mule.runtime.http.api.client.HttpClient httpClient = mock(org.mule.runtime.http.api.client.HttpClient.class);
    when(modelConnection.getHttpClient()).thenReturn(httpClient);

    HttpResponse mockResp = mock(HttpResponse.class);
    when(mockResp.getStatusCode()).thenReturn(500);
    when(mockResp.getEntity()).thenReturn(
                                          new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("error"
                                              .getBytes(StandardCharsets.UTF_8)));
    when(httpClient.send(any(), any())).thenReturn(mockResp);

    assertThrows(RuntimeException.class,
                 () -> service.generateImageEmbeddings(List.of(new byte[] {1, 2}), "model"));
  }

  @Test
  void embedImage_success() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("img-model");
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    String embeddingJson = "{\"embeddings\": [[0.7, 0.8, 0.9]]}";

    org.mule.runtime.http.api.client.HttpClient httpClient = mock(org.mule.runtime.http.api.client.HttpClient.class);
    when(modelConnection.getHttpClient()).thenReturn(httpClient);

    HttpResponse mockResp = mock(HttpResponse.class);
    when(mockResp.getStatusCode()).thenReturn(200);
    when(mockResp.getEntity()).thenReturn(
                                          new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(embeddingJson
                                              .getBytes(StandardCharsets.UTF_8)));
    when(httpClient.send(any(), any())).thenReturn(mockResp);

    Response<Embedding> resp = service.embedImage(new byte[] {1, 2, 3});
    assertNotNull(resp);
    assertNotNull(resp.content());
    assertArrayEquals(new float[] {0.7f, 0.8f, 0.9f}, resp.content().vector(), 1e-6f);
  }

  @Test
  void embedImage_emptyEmbeddings_throwsRuntimeException() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("img-model");
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    String embeddingJson = "{\"embeddings\": []}";

    org.mule.runtime.http.api.client.HttpClient httpClient = mock(org.mule.runtime.http.api.client.HttpClient.class);
    when(modelConnection.getHttpClient()).thenReturn(httpClient);

    HttpResponse mockResp = mock(HttpResponse.class);
    when(mockResp.getStatusCode()).thenReturn(200);
    when(mockResp.getEntity()).thenReturn(
                                          new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(embeddingJson
                                              .getBytes(StandardCharsets.UTF_8)));
    when(httpClient.send(any(), any())).thenReturn(mockResp);

    assertThrows(RuntimeException.class, () -> service.embedImage(new byte[] {1, 2}));
  }

  @Test
  void embedTextAndImage_delegatesToEmbedImage() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("img-model");
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    String embeddingJson = "{\"embeddings\": [[0.1, 0.2]]}";

    org.mule.runtime.http.api.client.HttpClient httpClient = mock(org.mule.runtime.http.api.client.HttpClient.class);
    when(modelConnection.getHttpClient()).thenReturn(httpClient);

    HttpResponse mockResp = mock(HttpResponse.class);
    when(mockResp.getStatusCode()).thenReturn(200);
    when(mockResp.getEntity()).thenReturn(
                                          new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(embeddingJson
                                              .getBytes(StandardCharsets.UTF_8)));
    when(httpClient.send(any(), any())).thenReturn(mockResp);

    Response<Embedding> resp = service.embedTextAndImage("hello", new byte[] {1, 2});
    assertNotNull(resp);
    assertArrayEquals(new float[] {0.1f, 0.2f}, resp.content().vector(), 1e-6f);
  }

  @Test
  void embedImages_success() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("img-model");
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    String embeddingJson = "{\"embeddings\": [[0.1], [0.2]], \"usage\": {\"total_tokens\": 5}}";

    org.mule.runtime.http.api.client.HttpClient httpClient = mock(org.mule.runtime.http.api.client.HttpClient.class);
    when(modelConnection.getHttpClient()).thenReturn(httpClient);

    HttpResponse mockResp = mock(HttpResponse.class);
    when(mockResp.getStatusCode()).thenReturn(200);
    when(mockResp.getEntity()).thenReturn(
                                          new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(embeddingJson
                                              .getBytes(StandardCharsets.UTF_8)));
    when(httpClient.send(any(), any())).thenReturn(mockResp);

    Response<List<Embedding>> resp = service.embedImages(List.of(new byte[] {1}, new byte[] {2}));
    assertNotNull(resp);
    assertEquals(2, resp.content().size());
  }

  @Test
  void embedImages_throwsOnApiFailure() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("img-model");
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);

    org.mule.runtime.http.api.client.HttpClient httpClient = mock(org.mule.runtime.http.api.client.HttpClient.class);
    when(modelConnection.getHttpClient()).thenReturn(httpClient);

    HttpResponse mockResp = mock(HttpResponse.class);
    when(mockResp.getStatusCode()).thenReturn(500);
    when(mockResp.getEntity()).thenReturn(
                                          new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("error"
                                              .getBytes(StandardCharsets.UTF_8)));
    when(httpClient.send(any(), any())).thenReturn(mockResp);

    assertThrows(RuntimeException.class,
                 () -> service.embedImages(List.of(new byte[] {1})));
  }

  @Test
  void embedTexts_multipleTexts_returnsMultipleEmbeddings() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
    String embeddingJson = "{" +
        "\"embeddings\": [[0.1, 0.2], [0.3, 0.4]]," +
        "\"usage\": {\"total_tokens\": 42}" +
        "}";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      when(modelConnection.getHttpClient()).thenReturn(null);
      when(modelConnection.getApiKey()).thenReturn("key");
      when(modelConnection.getTimeout()).thenReturn(1000L);
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(embeddingJson);
      Response<List<Embedding>> resp = service.embedTexts(List.of("foo", "bar"));
      assertNotNull(resp);
      assertEquals(2, resp.content().size());
      assertNotNull(resp.tokenUsage());
      assertEquals(42, resp.tokenUsage().inputTokenCount());
    }
  }
}
