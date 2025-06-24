/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.model.mistralai;

import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Alias("mistralAI")
@DisplayName("Mistral AI")
public class MistralAIModelConnection implements BaseTextModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(MistralAIModelConnection.class);
    private static final String MODELS_ENDPOINT = "https://api.mistral.ai/v1/models";
    private static final String EMBEDDINGS_ENDPOINT = "https://api.mistral.ai/v1/embeddings";

    private final String apiKey;
    private final HttpClient httpClient;
    private final long timeout;
    private final ObjectMapper objectMapper;

    public MistralAIModelConnection(String apiKey, long timeout, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getEmbeddingModelService() {
        return Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI;
    }


    @Override
    public void disconnect() {
        // HttpClient lifecycle is managed by the provider
    }

    @Override
    public void validate() {
        try {
            validateModelsAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to validate connection to Mistral AI", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
        }
    }

    private CompletableFuture<Void> validateModelsAsync() {
        return HttpRequestHelper.executeGetRequest(httpClient, MODELS_ENDPOINT, buildAuthHeaders(), (int) timeout)
                .thenAccept(response -> {
                    if (response.getStatusCode() != 200) {
                        handleErrorResponse(response, "Failed to connect to Mistral AI.");
                    }
                });
    }

    @Override
    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be null or empty");
        }
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty");
        }

        try {
            return generateTextEmbeddingsAsync(inputs, modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateTextEmbeddingsAsync(List<String> inputs, String modelName) {
        try {
            byte[] body = buildEmbeddingsPayload(inputs, modelName);
            return HttpRequestHelper.executePostRequest(httpClient, EMBEDDINGS_ENDPOINT, buildAuthHeaders(), body, (int) timeout)
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

    private byte[] buildEmbeddingsPayload(List<String> inputs, String modelName) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("input", inputs);
        return objectMapper.writeValueAsBytes(requestBody);
    }

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.apiKey);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes());
            String errorMsg = String.format("%s. Error (HTTP %d): %s", message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
