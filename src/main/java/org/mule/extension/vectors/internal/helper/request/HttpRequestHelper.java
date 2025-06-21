package org.mule.extension.vectors.internal.helper.request;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpRequestHelper {

    public static CompletableFuture<HttpResponse> executeGetRequest(HttpClient httpClient, String url, Map<String, String> headers, int timeout) {
        HttpRequest request = buildGetRequest(url, headers);
        HttpRequestOptions options = getRequestOptions(timeout);
        return httpClient.sendAsync(request, options);
    }

    public static CompletableFuture<HttpResponse> executePostRequest(HttpClient httpClient, String url, Map<String, String> headers, byte[] body, int timeout) {
        HttpRequest request = buildPostRequest(url, headers, body);
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

    private static MultiMap<String, String> getDefaultHeaders() {
        return new MultiMap<>(Map.of("Content-Type", "application/json", "Accept", "application/json"));
    }

    private static HttpRequestOptions getRequestOptions(int timeout) {
        return HttpRequestOptions.builder()
                .responseTimeout(timeout)
                .followsRedirect(true)
                .build();
    }
} 
