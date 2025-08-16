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
}
