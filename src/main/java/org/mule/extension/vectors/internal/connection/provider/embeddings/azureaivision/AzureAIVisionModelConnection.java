/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision;

import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AzureAIVisionModelConnection implements BaseModelConnection {

    private final String endpoint;
    private final String apiKey;
    private final String apiVersion;
    private final long timeout;
    private final HttpClient httpClient;

    public AzureAIVisionModelConnection(String endpoint, String apiKey, String apiVersion, long timeout, HttpClient httpClient) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.timeout = timeout;
        this.httpClient = httpClient;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public String getEmbeddingModelService() {
        return Constants.EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION;
    }

    @Override
    public void disconnect() {
        // HttpClient lifecycle is managed by the provider
    }

    @Override
    public void validate() {
        try {
            validateCredentialsAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to validate connection to Azure AI Vision", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
        }
    }

    private CompletableFuture<Void> validateCredentialsAsync() {
        String url = buildUrlWithParams(endpoint + "/computervision/retrieval:vectorizeImage",
                Map.of("api-version", apiVersion, "model-version", ""));
        
        return HttpRequestHelper.executePostRequest(httpClient, url, buildHeaders("application/json"), new byte[0], (int) timeout)
                .thenAccept(response -> {
                    // We expect a 400 Bad Request if credentials are ok, because the model-version is empty.
                    if (response.getStatusCode() != 400) {
                        handleErrorResponse(response, "Failed to validate credentials");
                    }
                });
    }

    String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl);
        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> {
                try {
                    url.append(URLEncoder.encode(key, StandardCharsets.UTF_8.name()))
                       .append("=")
                       .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                       .append("&");
                } catch (UnsupportedEncodingException e) {
                    // This should not happen with UTF-8
                    throw new ModuleException("Failed to encode URL parameters", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e);
                }
            });
            url.setLength(url.length() - 1); // Remove last '&'
        }
        return url.toString();
    }
    
    Map<String, String> buildHeaders(String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Ocp-Apim-Subscription-Key", apiKey);
        return headers;
    }
    
    String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
            String errorMsg = String.format("%s. Azure AI Vision API error (HTTP %d): %s",
                    message, response.getStatusCode(), errorBody);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
