/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.model.azureaivision;

import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.BaseImageModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AzureAIVisionModelConnection implements BaseTextModelConnection, BaseImageModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAIVisionModelConnection.class);

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

    @Override
    public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
        LOGGER.debug("Embedding images, Model name: {}", modelName);
        if (imageBytesList.size() != 1) {
            throw new UnsupportedOperationException("Azure AI Vision only supports embedding one image at a time");
        }
        try {
            return generateImageEmbeddingsAsync(imageBytesList.get(0), modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to embed image", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
    
    private CompletableFuture<String> generateImageEmbeddingsAsync(byte[] imageBytes, String modelName) {
        String url = buildUrlWithParams(endpoint + "/computervision/retrieval:vectorizeImage",
                Map.of("api-version", apiVersion, "model-version", modelName));
        Map<String, String> headers = buildHeaders("application/octet-stream");

        return HttpRequestHelper.executePostRequest(httpClient, url, headers, imageBytes, (int) timeout)
                .thenApply(this::handleEmbeddingResponse);
    }
    
    @Override
    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        LOGGER.debug("Embedding texts, Model name: {}", modelName);
        if (inputs.size() != 1) {
            throw new UnsupportedOperationException("Azure AI Vision only supports embedding one text at a time");
        }
        try {
            return generateTextEmbeddingsAsync(inputs.get(0), modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to embed text", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateTextEmbeddingsAsync(String text, String modelName) {
        String url = buildUrlWithParams(endpoint + "/computervision/retrieval:vectorizeText",
                Map.of("api-version", apiVersion, "model-version", modelName));
        byte[] payload = new JSONObject().put("text", text).toString().getBytes(StandardCharsets.UTF_8);

        return HttpRequestHelper.executePostRequest(httpClient, url, buildHeaders("application/json"), payload, (int) timeout)
                .thenApply(this::handleEmbeddingResponse);
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        if (response.getStatusCode() != 200) {
            return handleErrorResponse(response, "Failed to generate embedding");
        }
        try {
            return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
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
    
    private Map<String, String> buildHeaders(String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Ocp-Apim-Subscription-Key", apiKey);
        return headers;
    }
    
    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
            String errorMsg = String.format("%s. Azure AI Vision API error (HTTP %d): %s",
                    message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
