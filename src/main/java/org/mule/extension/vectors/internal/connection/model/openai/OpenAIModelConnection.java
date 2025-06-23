/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.model.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.api.connection.ConnectionException;
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

@Alias("openAI")
@DisplayName("OpenAI")
public class OpenAIModelConnection implements BaseTextModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIModelConnection.class);
    private static final String MODELS_ENDPOINT = "https://api.openai.com/v1/models";
    private static final String EMBEDDINGS_ENDPOINT = "https://api.openai.com/v1/embeddings";

    private final String apiKey;
    private final HttpClient httpClient;
    private final long timeout;
    private final ObjectMapper objectMapper;

    public OpenAIModelConnection(String apiKey, long timeout, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getEmbeddingModelService() {
        return Constants.EMBEDDING_MODEL_SERVICE_OPENAI;
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
            throw new ModuleException("Failed to validate connection to OpenAI.", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
        }
    }

    private CompletableFuture<Void> getModelsAsync() {
        return HttpRequestHelper.executeGetRequest(httpClient, MODELS_ENDPOINT, buildAuthHeaders(), (int) timeout)
                .thenAccept(response -> {
                    int statusCode = response.getStatusCode();
                    if (statusCode == 401 || statusCode == 403) {
                        LOGGER.error("Authentication failed. Please check your credentials.");
                        throw new ModuleException("Invalid credentials", MuleVectorsErrorType.INVALID_CONNECTION);
                    }
                    if (statusCode != 200) {
                        handleErrorResponse(response, "Failed to validate credentials");
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
            byte[] jsonBody = buildEmbeddingsPayload(inputs, modelName);
            Map<String, String> headers = buildAuthHeaders();
            headers.put("Content-Type", "application/json");

            return HttpRequestHelper.executePostRequest(httpClient, EMBEDDINGS_ENDPOINT, headers, jsonBody, (int) timeout)
                    .thenApply(response -> {
                        if (response.getStatusCode() != 200) {
                            return handleErrorResponse(response, "Error generating embeddings");
                        }
                        try {
                            return new String(response.getEntity().getBytes());
                        } catch (IOException e) {
                            throw new ModuleException("Failed to read response entity", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
                        }
                    });
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new ModuleException("Failed to create request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
        }
    }
    
    private byte[] buildEmbeddingsPayload(List<String> inputs, String modelName) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", inputs);
        requestBody.put("model", modelName);
        return objectMapper.writeValueAsBytes(requestBody);
    }

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        return headers;
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
