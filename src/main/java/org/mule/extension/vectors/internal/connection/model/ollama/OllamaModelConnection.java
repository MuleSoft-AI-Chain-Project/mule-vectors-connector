/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.model.ollama;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Alias("ollama")
@DisplayName("Ollama")
public class OllamaModelConnection implements BaseTextModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaModelConnection.class);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final long timeout;
    private final ObjectMapper objectMapper;
    private static final String MODELS_ENDPOINT = "/api/tags";
    private static final String EMBEDDINGS_ENDPOINT = "/api/embeddings";

    public OllamaModelConnection(String baseUrl, long timeout, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public String getEmbeddingModelService() {
        return Constants.EMBEDDING_MODEL_SERVICE_OLLAMA;
    }


    @Override
    public void disconnect() {
        // HttpClient lifecycle is managed by the provider
    }

    @Override
    public void validate() {
        try {
            getModelsAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to validate connection to Ollama.", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
        }
    }

    private CompletableFuture<Void> getModelsAsync() {
        return HttpRequestHelper.executeGetRequest(httpClient, baseUrl + MODELS_ENDPOINT, null, (int) timeout)
                .thenAccept(response -> {
                    if (response.getStatusCode() != 200) {
                        handleErrorResponse(response, "Error getting models");
                    }
                });
    }

    @Override
    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be null or empty");
        }
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Ollama embeddings API accepts only one input at a time");
        }
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty");
        }

        try {
            return generateTextEmbeddingsAsync(inputs.get(0), modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateTextEmbeddingsAsync(String input, String modelName) {
        try {
            byte[] body = buildEmbeddingsPayload(input, modelName);
            return HttpRequestHelper.executePostRequest(httpClient, baseUrl + EMBEDDINGS_ENDPOINT, buildJsonHeaders(), body, (int) timeout)
                    .thenApply(this::handleEmbeddingResponse);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new ModuleException("Failed to create request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
        }
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        if (response.getStatusCode() != 200) {
            return handleErrorResponse(response, "Error generating embeddings");
        }
        try {
            return new String(response.getEntity().getBytes());
        } catch (IOException e) {
            throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private byte[] buildEmbeddingsPayload(String input, String modelName) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("prompt", input); // Ollama takes one input at a time
        return objectMapper.writeValueAsBytes(requestBody);
    }

    private Map<String, String> buildJsonHeaders() {
        return Map.of("Content-Type", "application/json");
    }

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes());
            String errorMsg = String.format("%s. Status: %d - %s", message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
