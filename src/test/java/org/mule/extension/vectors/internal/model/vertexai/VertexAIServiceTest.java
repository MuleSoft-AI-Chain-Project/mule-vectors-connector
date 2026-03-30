package org.mule.extension.vectors.internal.model.vertexai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.vertexai.VertexAIService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class VertexAIServiceTest {

  @Mock
  VertexAIModelConnection modelConnection;
  @Mock
  EmbeddingModelParameters modelParameters;
  @Mock
  HttpResponse httpResponse;
  VertexAIService service;

  @BeforeEach
  void setUp() {
    modelConnection = mock(VertexAIModelConnection.class);
    modelParameters = mock(EmbeddingModelParameters.class);
    httpResponse = mock(HttpResponse.class);
    when(modelConnection.getLocation()).thenReturn("us-central1");
    when(modelConnection.getProjectId()).thenReturn("project");
    when(modelConnection.getHttpClient()).thenReturn(null);
    when(modelConnection.getTotalTimeout()).thenReturn(1000L);
    when(modelConnection.getBatchSize()).thenReturn(16);
    when(modelConnection.getObjectMapper()).thenReturn(new ObjectMapper());
    when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
    service = new VertexAIService(modelConnection, modelParameters);
  }

  @Test
  void constructor_setsFieldsCorrectly() throws Exception {
    assertThat(service).isNotNull();
    var connField = VertexAIService.class.getDeclaredField("vertexAIModelConnection");
    connField.setAccessible(true);
    var paramsField = VertexAIService.class.getDeclaredField("embeddingModelParameters");
    paramsField.setAccessible(true);
    assertThat(connField.get(service)).isEqualTo(modelConnection);
    assertThat(paramsField.get(service)).isEqualTo(modelParameters);
  }

  @Test
  void embedTexts_success() throws Exception {
    List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    when(httpResponse.getStatusCode()).thenReturn(200);
    when(httpResponse.getEntity())
        .thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("{\"predictions\": [{\"embeddings\": {\"values\": [0.1, 0.2]}}]}"
            .getBytes(StandardCharsets.UTF_8)));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));
      Response<List<Embedding>> response = service.embedTexts(List.of("foo"));
      assertThat(response.content()).hasSize(1);
      assertThat(response.content().get(0).vector()).containsExactly(0.1f, 0.2f);
    }
  }

  @Test
  void embedTexts_throwsOnError() throws Exception {
    List<String> texts = List.of("foo");
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenThrow(new ModuleException("fail", MuleVectorsErrorType.AI_SERVICES_FAILURE));
      assertThatThrownBy(() -> service.embedTexts(texts))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("fail");
    }
  }

  @Test
  void embedTexts_handlesEmptyResponse() throws Exception {
    List<String> texts = List.of("foo");
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      // Simulate a response with no 'predictions' key
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(new JSONObject("{}")));
      assertThatThrownBy(() -> service.embedTexts(texts))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to generate embeddings");
    }
  }

  @Test
  void embedTexts_handlesNon200Response() throws Exception {
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    when(httpResponse.getStatusCode()).thenReturn(401);
    when(httpResponse.getEntity())
        .thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("Unauthorized"
            .getBytes(StandardCharsets.UTF_8)));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));
      List<String> texts = List.of("foo");
      assertThatThrownBy(() -> service.embedTexts(texts))
          .isInstanceOf(ModuleException.class);
    }
  }

  @Test
  void embedTexts_handlesInterruptedException() throws Exception {
    CompletableFuture<String> future = new CompletableFuture<>();
    future.completeExceptionally(new InterruptedException("interrupted"));
    when(modelConnection.getOrRefreshToken()).thenReturn(future);
    List<String> texts = List.of("foo");
    assertThatThrownBy(() -> service.embedTexts(texts))
        .isInstanceOf(ModuleException.class);
    Thread.interrupted();
  }

  @Test
  void embedTexts_propagatesModuleException() throws Exception {
    CompletableFuture<String> future = new CompletableFuture<>();
    future.completeExceptionally(new ModuleException("auth", MuleVectorsErrorType.AI_SERVICES_FAILURE));
    when(modelConnection.getOrRefreshToken()).thenReturn(future);
    List<String> texts = List.of("foo");
    assertThatThrownBy(() -> service.embedTexts(texts))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("auth");
  }

  @Test
  void embedTexts_batchProcessing() throws Exception {
    when(modelConnection.getBatchSize()).thenReturn(1);
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    when(httpResponse.getStatusCode()).thenReturn(200);
    when(httpResponse.getEntity())
        .thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(
                                                                                    "{\"predictions\": [{\"embeddings\": {\"values\": [0.1]}}]}"
                                                                                        .getBytes(StandardCharsets.UTF_8)));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));
      Response<List<Embedding>> response = service.embedTexts(List.of("foo", "bar"));
      assertThat(response.content()).hasSize(2);
    }
  }

  @Test
  void buildTextPayload_usesContentKey_forTextModels() throws Exception {
    when(modelParameters.getEmbeddingModelName()).thenReturn("text-embedding");
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    when(httpResponse.getStatusCode()).thenReturn(200);
    when(httpResponse.getEntity())
        .thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(
                                                                                    "{\"predictions\": [{\"embeddings\": {\"values\": [0.1]}}]}"
                                                                                        .getBytes(StandardCharsets.UTF_8)));
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));
      Response<List<Embedding>> response = service.embedTexts(List.of("foo"));
      assertThat(response.content()).hasSize(1);
    }
  }

  @Test
  void handleErrorResponse_IOExceptionReadingBody() throws Exception {
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    HttpResponse failResponse = mock(HttpResponse.class);
    when(failResponse.getStatusCode()).thenReturn(500);
    org.mule.runtime.http.api.domain.entity.HttpEntity entity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
    when(entity.getBytes()).thenThrow(new java.io.IOException("read fail"));
    when(failResponse.getEntity()).thenReturn(entity);
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(failResponse));
      List<String> texts = List.of("foo");
      assertThatThrownBy(() -> service.embedTexts(texts))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to read error response body");
    }
  }

  @Test
  void handleEmbeddingResponse_IOExceptionReadingBody() throws Exception {
    when(modelConnection.getOrRefreshToken()).thenReturn(CompletableFuture.completedFuture("token"));
    HttpResponse okResponse = mock(HttpResponse.class);
    when(okResponse.getStatusCode()).thenReturn(200);
    org.mule.runtime.http.api.domain.entity.HttpEntity entity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
    when(entity.getBytes()).thenThrow(new java.io.IOException("read fail"));
    when(okResponse.getEntity()).thenReturn(entity);
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(okResponse));
      List<String> texts = List.of("foo");
      assertThatThrownBy(() -> service.embedTexts(texts))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to read embedding response");
    }
  }
}
