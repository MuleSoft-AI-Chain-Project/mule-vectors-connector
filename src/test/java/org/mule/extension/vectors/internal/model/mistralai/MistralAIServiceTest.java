package org.mule.extension.vectors.internal.model.mistralai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.mistralai.MistralAIService;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
class MistralAIServiceTest {

  @Mock
  MistralAIModelConnection connection;
  @Mock
  EmbeddingModelParameters params;
  MistralAIService service;

  @BeforeEach
  void setup() {
    service = new MistralAIService(connection, params);
  }

  @Test
  void embedTexts_returnsEmbeddings() throws Exception {
    when(params.getEmbeddingModelName()).thenReturn("test-model");
    List<TextSegment> segments =
        List.of(dev.langchain4j.data.segment.TextSegment.from("foo"), dev.langchain4j.data.segment.TextSegment.from("bar"));
    JSONObject fakeUsage = new JSONObject().put("total_tokens", 42);
    JSONArray fakeData = new JSONArray()
        .put(new JSONObject().put("embedding", new JSONArray().put(0.1).put(0.2)))
        .put(new JSONObject().put("embedding", new JSONArray().put(0.3).put(0.4)));
    JSONObject fakeResponse = new JSONObject().put("usage", fakeUsage).put("data", fakeData);
    String responseString = fakeResponse.toString();
    when(connection.getTimeout()).thenReturn(1000L);
    when(connection.getApiKey()).thenReturn("key");
    when(connection.getHttpClient()).thenReturn(null);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(responseString);
      Response<List<Embedding>> resp = service.embedTexts(List.of("foo"));
      assertNotNull(resp);
      assertEquals(2, resp.content().size());
      assertEquals(0.1f, resp.content().get(0).vectorAsList().get(0));
      assertEquals(0.4f, resp.content().get(1).vectorAsList().get(1));
      assertNotNull(resp.tokenUsage());
    }
  }

  @Test
  void embedTexts_throwsOnErrorResponse() throws Exception {
    when(params.getEmbeddingModelName()).thenReturn("test-model");
    List<TextSegment> segments = List.of(dev.langchain4j.data.segment.TextSegment.from("foo"));
    when(connection.getTimeout()).thenReturn(1000L);
    when(connection.getApiKey()).thenReturn("key");
    when(connection.getHttpClient()).thenReturn(null);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenThrow(new org.mule.runtime.extension.api.exception.ModuleException("Mistral AI API error (HTTP 500): error",
                                                                                  org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_FAILURE));
      assertThrows(org.mule.runtime.extension.api.exception.ModuleException.class, () -> service.embedTexts(List.of("foo")));
    }
  }

  @Test
  void generateTextEmbeddings_throwsOnNullOrEmptyInput() {
    List<String> emptyList = List.of();
    List<String> fooList = List.of("foo");
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(null, "model"));
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(emptyList, "model"));
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(fooList, null));
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(fooList, ""));
  }

  @Test
  void buildEmbeddingsPayload_serializesInput() throws Exception {
    List<String> inputs = List.of("foo", "bar");
    String modelName = "test-model";
    byte[] payload = service.buildEmbeddingsPayload(inputs, modelName);
    String json = new String(payload, StandardCharsets.UTF_8);
    assertTrue(json.contains("foo"));
    assertTrue(json.contains("bar"));
    assertTrue(json.contains("test-model"));
  }

  @Test
  void buildAuthHeaders_containsAuthorization() {
    when(connection.getApiKey()).thenReturn("key");
    Map<String, String> headers = service.buildAuthHeaders();
    assertEquals("Bearer key", headers.get("Authorization"));
    assertEquals("application/json", headers.get("Content-Type"));
    assertEquals("application/json", headers.get("Accept"));
  }
}
