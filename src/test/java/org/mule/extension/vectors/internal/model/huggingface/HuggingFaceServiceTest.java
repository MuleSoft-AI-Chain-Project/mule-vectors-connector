package org.mule.extension.vectors.internal.model.huggingface;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.huggingface.HuggingFaceService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HuggingFaceServiceTest {

  @Mock
  HuggingFaceModelConnection connection;
  @Mock
  EmbeddingModelParameters params;
  HuggingFaceService service;

  @BeforeEach
  void setup() {
    service = new HuggingFaceService(connection, params);
  }

  @Test
  void embedTexts_returnsEmbeddings() throws Exception {
    when(params.getEmbeddingModelName()).thenReturn("test-model");
    List<TextSegment> segments =
        List.of(dev.langchain4j.data.segment.TextSegment.from("foo"), dev.langchain4j.data.segment.TextSegment.from("bar"));
    String fakeResponse = "[[0.1,0.2],[0.3,0.4]]";
    when(connection.getTimeout()).thenReturn(1000L);
    when(connection.getApiKey()).thenReturn("key");
    when(connection.getHttpClient()).thenReturn(null);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpResponse mockResponse = mock(HttpResponse.class);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(mockResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(fakeResponse);
      Response<List<Embedding>> resp = service.embedTexts(segments);
      assertNotNull(resp);
      assertEquals(2, resp.content().size());
      assertEquals(0.1f, resp.content().get(0).vectorAsList().get(0));
      assertEquals(0.4f, resp.content().get(1).vectorAsList().get(1));
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
          .thenThrow(new ModuleException("Hugging Face API error (HTTP 500): error", MuleVectorsErrorType.AI_SERVICES_FAILURE));
      assertThrows(ModuleException.class, () -> service.embedTexts(segments));
    }
  }

  @Test
  void generateTextEmbeddings_throwsOnNullOrEmptyInput() {
    List<String> emptyList = List.of();
    List<String> fooList = List.of("foo");
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(null, "model"));
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(emptyList, "model"));
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(fooList, null));
    assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(fooList, " "));
  }

  @Test
  void buildEmbeddingsPayload_serializesInput() throws Exception {
    List<String> inputs = List.of("foo", "bar");
    byte[] payload = service.buildEmbeddingsPayload(inputs);
    String json = new String(payload, StandardCharsets.UTF_8);
    assertTrue(json.contains("foo"));
    assertTrue(json.contains("bar"));
    assertTrue(json.contains("inputs"));
  }

  @Test
  void buildAuthHeaders_containsAuthorization() {
    when(connection.getApiKey()).thenReturn("key");
    Map<String, String> headers = service.buildAuthHeaders();
    assertEquals("Bearer key", headers.get("Authorization"));
  }
}
