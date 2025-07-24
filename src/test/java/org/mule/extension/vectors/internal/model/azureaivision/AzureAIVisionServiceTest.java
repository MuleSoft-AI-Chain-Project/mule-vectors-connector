package org.mule.extension.vectors.internal.model.azureaivision;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.azureaivision.AzureAIVisionService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class AzureAIVisionServiceTest {

  @Mock
  AzureAIVisionModelConnection modelConnection;
  @Mock
  EmbeddingModelParameters modelParameters;
  @Mock
  HttpResponse httpResponse;
  @Mock
  org.mule.runtime.http.api.domain.entity.HttpEntity httpEntity;

  AzureAIVisionService service;

  @BeforeEach
  void setUp() {
    modelConnection = mock(AzureAIVisionModelConnection.class);
    modelParameters = mock(EmbeddingModelParameters.class);
    httpEntity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
    httpResponse = mock(HttpResponse.class);
    when(modelConnection.getEndpoint()).thenReturn("https://test-endpoint");
    when(modelConnection.getApiVersion()).thenReturn("v1");
    when(modelConnection.getApiKey()).thenReturn("key");
    when(modelConnection.getTimeout()).thenReturn(1000L);
    when(modelConnection.getHttpClient()).thenReturn(null); // Not used directly in test
    when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
    service = new AzureAIVisionService(modelConnection, modelParameters);
  }

  @Test
  void constructor_setsFields() {
    assertThat(service).isNotNull();
  }

  @Test
  void generateTextEmbeddings_throwsIfMultipleInputs() {
    List<String> input = List.of("a", "b");
    assertThatThrownBy(() -> service.generateTextEmbeddings(input, "model"))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("only supports embedding one text");
  }

  @Test
  void generateTextEmbeddings_success() throws Exception {
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      String fakeResponse = new JSONObject().put("vector", List.of(0.1, 0.2, 0.3)).toString();
      httpResponse = mock(HttpResponse.class);
      httpEntity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
      when(httpResponse.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(httpEntity.getBytes()).thenReturn(fakeResponse.getBytes(StandardCharsets.UTF_8));
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(fakeResponse);
      Object result = service.generateTextEmbeddings(List.of("hello"), "test-model");
      assertThat(result).isInstanceOf(String.class);
      assertThat(result.toString()).contains("vector");
    }
  }

  @Test
  void generateImageEmbeddings_throwsIfMultipleInputs() {
    List<byte[]> input = List.of(new byte[1], new byte[2]);
    assertThatThrownBy(() -> service.generateImageEmbeddings(input, "model"))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("only supports embedding one image");
  }

  @Test
  void generateImageEmbeddings_success() throws Exception {
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      String fakeResponse = new JSONObject().put("vector", List.of(0.1, 0.2, 0.3)).toString();
      httpResponse = mock(HttpResponse.class);
      httpEntity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
      when(httpResponse.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      when(httpEntity.getBytes()).thenReturn(fakeResponse.getBytes(StandardCharsets.UTF_8));
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(fakeResponse);
      Object result = service.generateImageEmbeddings(List.of(new byte[1]), "test-model");
      assertThat(result).isInstanceOf(String.class);
      assertThat(result.toString()).contains("vector");
    }
  }

  @Test
  void buildUrlWithParams_encodesParams() throws Exception {
    var method = AzureAIVisionService.class.getDeclaredMethod("buildUrlWithParams", String.class, Map.class);
    method.setAccessible(true);
    String url = (String) method.invoke(service, "http://base", Map.of("a", "b c"));
    assertThat(url).contains("a=b+c");
  }

  @Test
  void handleEmbeddingResponse_handlesNon200() throws Exception {
    httpResponse = mock(HttpResponse.class);
    httpEntity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
    when(httpResponse.getStatusCode()).thenReturn(400);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getBytes()).thenReturn("fail".getBytes(StandardCharsets.UTF_8));
    // Use reflection to access private method
    var method = AzureAIVisionService.class.getDeclaredMethod("handleEmbeddingResponse", HttpResponse.class);
    method.setAccessible(true);
    assertThatThrownBy(() -> method.invoke(service, httpResponse))
        .hasCauseInstanceOf(ModuleException.class);
  }

  @Test
  void embedTexts_success() throws Exception {
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      // Arrange
      List<TextSegment> segments = List.of(
                                           new TextSegment("foo", new dev.langchain4j.data.document.Metadata()),
                                           new TextSegment("bar", new dev.langchain4j.data.document.Metadata()));
      String fakeResponse = "{\"vector\": [0.1, 0.2]}";
      when(httpEntity.getBytes()).thenReturn(fakeResponse.getBytes());
      when(httpResponse.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(httpEntity);
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));
      helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
          .thenReturn(fakeResponse);
      // Act
      List<Embedding> result = service.embedTexts(segments).content();
      // Assert
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      assertThat(result.get(0).vector()).containsExactly(0.1f, 0.2f);
      assertThat(result.get(1).vector()).containsExactly(0.1f, 0.2f);
    }
  }
}
