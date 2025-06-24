/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.model.nomic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.extension.vectors.internal.connection.model.BaseImageModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Alias("nomic")
@DisplayName("Nomic")
public class NomicModelConnection implements BaseTextModelConnection, BaseImageModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(NomicModelConnection.class);
    private static final String BASE_URL = "https://api-atlas.nomic.ai/v1/";
    private static final String TEXT_EMBEDDING_URL = BASE_URL + "embedding/text";
    private static final String IMAGE_EMBEDDING_URL = BASE_URL + "embedding/image";

    private final String apiKey;
    private final long timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NomicModelConnection(String apiKey, long timeout, HttpClient httpClient) {
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
        return Constants.EMBEDDING_MODEL_SERVICE_NOMIC;
    }


    @Override
    public void disconnect() {
        // HttpClient lifecycle is managed by the provider
    }

    @Override
    public void validate() {
        try {
            validateConnectionAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to validate connection to Nomic", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
        }
    }

    private CompletableFuture<Void> validateConnectionAsync() {
        try {
            byte[] body = buildValidationPayload();
            return HttpRequestHelper.executePostRequest(httpClient, TEXT_EMBEDDING_URL, buildAuthHeaders(), body, (int) timeout)
                    .thenAccept(response -> {
                        if (response.getStatusCode() != 200) {
                            handleErrorResponse(response, "Failed to validate connection to Nomic");
                        }
                    });
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new ModuleException("Failed to build validation request", MuleVectorsErrorType.INVALID_CONNECTION, e));
        }
    }

    @Override
    public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
        if (imageBytesList == null || imageBytesList.isEmpty()) {
            throw new IllegalArgumentException("No images provided for embedding.");
        }
        try {
            return generateImageEmbeddingsAsync(imageBytesList, modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate image embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateImageEmbeddingsAsync(List<byte[]> imageBytesList, String modelName) {
        List<HttpPart> parts = buildImageMultipartPayload(imageBytesList, modelName);
        return HttpRequestHelper.executeMultipartPostRequest(httpClient, IMAGE_EMBEDDING_URL, buildAuthHeaders(), parts, (int) timeout)
                .thenApply(this::handleEmbeddingResponse);
    }


    @Override
    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        try {
            return generateTextEmbeddingsAsync(inputs, modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate text embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
    
    private CompletableFuture<String> generateTextEmbeddingsAsync(List<String> inputs, String modelName) {
        try {
            byte[] body = buildTextEmbeddingsPayload(inputs, modelName);
            return HttpRequestHelper.executePostRequest(httpClient, TEXT_EMBEDDING_URL, buildAuthHeaders(), body, (int) timeout)
                    .thenApply(this::handleEmbeddingResponse);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new ModuleException("Failed to create text embedding request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
        }
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        if (response.getStatusCode() != 200) {
            return handleErrorResponse(response, "Failed to generate embeddings");
        }
        try {
            return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private byte[] buildValidationPayload() throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("task_type", "search_document");
        requestBody.put("texts", new ArrayList<>());
        requestBody.put("max_tokens_per_text", 0);
        return objectMapper.writeValueAsBytes(requestBody);
    }
    
    private List<HttpPart> buildImageMultipartPayload(List<byte[]> imageBytesList, String modelName) {
        List<HttpPart> parts = new ArrayList<>();
        byte[] modelBytes = modelName.getBytes(StandardCharsets.UTF_8);
        parts.add(new HttpPart("model", modelBytes, "text/plain", modelBytes.length));

        int index = 0;
        for (byte[] imageBytes : imageBytesList) {
            String fileName = "image_" + index++ + ".png";
            parts.add(new HttpPart("images", fileName, imageBytes, "image/png", imageBytes.length));
        }
        return parts;
    }

    private byte[] buildTextEmbeddingsPayload(List<String> inputs, String modelName) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("texts", inputs);
        return objectMapper.writeValueAsBytes(requestBody);
    }

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        return headers;
    }

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
            String errorMsg = String.format("%s: HTTP %d. Error: %s", message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
