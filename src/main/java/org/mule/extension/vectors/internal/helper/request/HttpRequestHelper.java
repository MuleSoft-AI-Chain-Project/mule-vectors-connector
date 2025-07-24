package org.mule.extension.vectors.internal.helper.request;

import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.entity.multipart.MultipartHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpRequestHelper {

  private HttpRequestHelper() {}

  public static CompletableFuture<HttpResponse> executeGetRequest(HttpClient httpClient, String url, Map<String, String> headers,
                                                                  int timeout) {
    HttpRequest request = buildGetRequest(url, headers);
    HttpRequestOptions options = getRequestOptions(timeout);
    return httpClient.sendAsync(request, options);
  }

  public static CompletableFuture<HttpResponse> executePostRequest(HttpClient httpClient, String url, Map<String, String> headers,
                                                                   byte[] body, int timeout) {
    HttpRequest request = buildPostRequest(url, headers, body);
    HttpRequestOptions options = getRequestOptions(timeout);
    return httpClient.sendAsync(request, options);
  }

  public static CompletableFuture<HttpResponse> executeMultipartPostRequest(HttpClient httpClient, String url,
                                                                            Map<String, String> headers, List<HttpPart> parts,
                                                                            int timeout) {
    HttpRequest request = buildMultipartPostRequest(url, headers, parts);
    HttpRequestOptions options = getRequestOptions(timeout);
    return httpClient.sendAsync(request, options);
  }

  private static HttpRequest buildGetRequest(String url, Map<String, String> headers) {
    MultiMap<String, String> finalHeaders = new MultiMap<>();
    finalHeaders.putAll(getDefaultHeaders());
    if (headers != null) {
      finalHeaders.putAll(new MultiMap<>(headers));
    }

    return HttpRequest.builder()
        .method("GET")
        .uri(url)
        .headers(finalHeaders)
        .build();
  }

  private static HttpRequest buildPostRequest(String url, Map<String, String> headers, byte[] body) {
    MultiMap<String, String> finalHeaders = new MultiMap<>();
    finalHeaders.putAll(getDefaultHeaders());
    if (headers != null) {
      finalHeaders.putAll(new MultiMap<>(headers));
    }

    return HttpRequest.builder()
        .method("POST")
        .uri(url)
        .headers(finalHeaders)
        .entity(new ByteArrayHttpEntity(body))
        .build();
  }

  private static HttpRequest buildMultipartPostRequest(String url, Map<String, String> headers, List<HttpPart> parts) {
    MultiMap<String, String> finalHeaders = new MultiMap<>();
    if (headers != null) {
      finalHeaders.putAll(new MultiMap<>(headers));
    }

    return HttpRequest.builder()
        .method("POST")
        .uri(url)
        .headers(finalHeaders)
        .entity(new MultipartHttpEntity(parts))
        .build();
  }

  private static MultiMap<String, String> getDefaultHeaders() {
    return new MultiMap<>(Map.of("Content-Type", "application/json", "Accept", "application/json"));
  }

  private static HttpRequestOptions getRequestOptions(int timeout) {
    return HttpRequestOptions.builder()
        .responseTimeout(timeout)
        .followsRedirect(true)
        .build();
  }

  public static String handleEmbeddingResponse(HttpResponse response, String msg) {
    if (response.getStatusCode() != 200) {
      return handleErrorResponse(response, msg);
    }
    try {
      return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }

  private static String handleErrorResponse(HttpResponse response, String message) {
    try {
      String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
      String errorMsg = String.format("%s API error (HTTP %d): %s",
                                      message, response.getStatusCode(), errorBody);
      throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
    } catch (IOException e) {
      throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }
}
