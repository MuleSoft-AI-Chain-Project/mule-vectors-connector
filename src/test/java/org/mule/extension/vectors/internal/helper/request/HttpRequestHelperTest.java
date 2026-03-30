package org.mule.extension.vectors.internal.helper.request;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpRequestHelperTest {

  @Mock
  HttpClient httpClient;
  @Mock
  HttpResponse httpResponse;

  @Test
  void executeGetRequest_sendsGetAndReturnsFuture() {
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpRequestOptions.class))).thenReturn(future);

    CompletableFuture<HttpResponse> result =
        HttpRequestHelper.executeGetRequest(httpClient, "http://example.com", Map.of("X-Key", "val"), 5000);
    assertThat(result).isCompletedWithValue(httpResponse);
  }

  @Test
  void executeGetRequest_withNullHeaders() {
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpRequestOptions.class))).thenReturn(future);

    CompletableFuture<HttpResponse> result =
        HttpRequestHelper.executeGetRequest(httpClient, "http://example.com", null, 5000);
    assertThat(result).isCompletedWithValue(httpResponse);
  }

  @Test
  void executePostRequest_sendsPostWithBody() {
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpRequestOptions.class))).thenReturn(future);

    byte[] body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
    CompletableFuture<HttpResponse> result =
        HttpRequestHelper.executePostRequest(httpClient, "http://example.com/api", Map.of("Auth", "token"), body, 10000);
    assertThat(result).isCompletedWithValue(httpResponse);
  }

  @Test
  void executePostRequest_withNullHeaders() {
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpRequestOptions.class))).thenReturn(future);

    byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
    CompletableFuture<HttpResponse> result =
        HttpRequestHelper.executePostRequest(httpClient, "http://example.com/api", null, body, 5000);
    assertThat(result).isCompletedWithValue(httpResponse);
  }

  @Test
  void executeMultipartPostRequest_sendsMultipartPost() {
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpRequestOptions.class))).thenReturn(future);

    byte[] partBytes = "part-content".getBytes(StandardCharsets.UTF_8);
    HttpPart part = new HttpPart("field", partBytes, "text/plain", partBytes.length);
    CompletableFuture<HttpResponse> result = HttpRequestHelper.executeMultipartPostRequest(
                                                                                           httpClient,
                                                                                           "http://example.com/upload",
                                                                                           Map.of("Auth", "token"),
                                                                                           List.of(part), 10000);
    assertThat(result).isCompletedWithValue(httpResponse);
  }

  @Test
  void executeMultipartPostRequest_withNullHeaders() {
    CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
    when(httpClient.sendAsync(any(HttpRequest.class), any(HttpRequestOptions.class))).thenReturn(future);

    byte[] partBytes = "data".getBytes(StandardCharsets.UTF_8);
    HttpPart part = new HttpPart("field", partBytes, "text/plain", partBytes.length);
    CompletableFuture<HttpResponse> result = HttpRequestHelper.executeMultipartPostRequest(
                                                                                           httpClient,
                                                                                           "http://example.com/upload",
                                                                                           null, List.of(part), 5000);
    assertThat(result).isCompletedWithValue(httpResponse);
  }

  @Test
  void handleEmbeddingResponse_returns200Body() throws IOException {
    when(httpResponse.getStatusCode()).thenReturn(200);
    HttpEntity entity = mock(HttpEntity.class);
    when(entity.getBytes()).thenReturn("{\"result\":\"ok\"}".getBytes(StandardCharsets.UTF_8));
    when(httpResponse.getEntity()).thenReturn(entity);

    String result = HttpRequestHelper.handleEmbeddingResponse(httpResponse, "Test");
    assertThat(result).isEqualTo("{\"result\":\"ok\"}");
  }

  @Test
  void handleEmbeddingResponse_throwsOnNon200() throws IOException {
    when(httpResponse.getStatusCode()).thenReturn(500);
    HttpEntity entity = mock(HttpEntity.class);
    when(entity.getBytes()).thenReturn("Internal Server Error".getBytes(StandardCharsets.UTF_8));
    when(httpResponse.getEntity()).thenReturn(entity);

    assertThatThrownBy(() -> HttpRequestHelper.handleEmbeddingResponse(httpResponse, "Test"))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Test API error (HTTP 500)");
  }

  @Test
  void handleEmbeddingResponse_throwsOnIOExceptionReading200() throws IOException {
    when(httpResponse.getStatusCode()).thenReturn(200);
    HttpEntity entity = mock(HttpEntity.class);
    when(entity.getBytes()).thenThrow(new IOException("read fail"));
    when(httpResponse.getEntity()).thenReturn(entity);

    assertThatThrownBy(() -> HttpRequestHelper.handleEmbeddingResponse(httpResponse, "Test"))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Failed to read embedding response");
  }

  @Test
  void handleEmbeddingResponse_throwsOnIOExceptionReadingError() throws IOException {
    when(httpResponse.getStatusCode()).thenReturn(400);
    HttpEntity entity = mock(HttpEntity.class);
    when(entity.getBytes()).thenThrow(new IOException("read fail"));
    when(httpResponse.getEntity()).thenReturn(entity);

    assertThatThrownBy(() -> HttpRequestHelper.handleEmbeddingResponse(httpResponse, "Test"))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Failed to read error response body");
  }

  @Test
  void handleEmbeddingResponse_returnsErrorBodyIn4xxResponse() throws IOException {
    when(httpResponse.getStatusCode()).thenReturn(401);
    HttpEntity entity = mock(HttpEntity.class);
    when(entity.getBytes()).thenReturn("Unauthorized".getBytes(StandardCharsets.UTF_8));
    when(httpResponse.getEntity()).thenReturn(entity);

    assertThatThrownBy(() -> HttpRequestHelper.handleEmbeddingResponse(httpResponse, "Auth"))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Auth API error (HTTP 401)")
        .hasMessageContaining("Unauthorized");
  }
}
