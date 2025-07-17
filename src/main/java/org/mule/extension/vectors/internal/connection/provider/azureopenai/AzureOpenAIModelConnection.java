/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.provider.azureopenai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
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

public class AzureOpenAIModelConnection implements BaseModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIModelConnection.class);

    private final String endpoint;
    private final String apiKey;
    private final String apiVersion;
    private final long timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AzureOpenAIModelConnection(String endpoint, String apiKey, String apiVersion, long timeout, HttpClient httpClient) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.timeout = timeout;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
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
        return Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI;
    }

    @Override
    public void disconnect() {
        // HttpClient lifecycle is managed by the provider
    }

    @Override
    public void validate() {
        try {
            testCredentialsAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to validate connection to Azure Open AI", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
        }
    }

    private CompletableFuture<Void> testCredentialsAsync() {
        String url = buildUrlForDeployment("test-connection");
        byte[] body;
        try {
            body = buildTextEmbeddingPayload(List.of(""));
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }

        return HttpRequestHelper.executePostRequest(httpClient, url, buildHeaders(), body, (int) timeout)
                .thenAccept(response -> {
                    int statusCode = response.getStatusCode();
                    if (statusCode == 401 || statusCode == 403) {
                        LOGGER.error("Authentication failed. Please check your credentials.");
                        throw new ModuleException("Invalid credentials", MuleVectorsErrorType.INVALID_CONNECTION);
                    }
                    if (statusCode != 404 && statusCode != 400) {
                        handleErrorResponse(response, "Failed to validate credentials");
                    }
                });
    }

    private String buildUrlForDeployment(String deploymentName) {
        try {
            String encodedDeployment = URLEncoder.encode(deploymentName, StandardCharsets.UTF_8.name());
            String encodedApiVersion = URLEncoder.encode(apiVersion, StandardCharsets.UTF_8.name());
            return String.format("%s/openai/deployments/%s/embeddings?api-version=%s", endpoint, encodedDeployment, encodedApiVersion);
        } catch (UnsupportedEncodingException e) {
            throw new ModuleException("Failed to encode URL parameters", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e);
        }
    }

    private byte[] buildTextEmbeddingPayload(List<String> inputs) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", inputs);
        return objectMapper.writeValueAsBytes(requestBody);
    }
    
    private Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", apiKey);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
            String errorMsg = String.format("%s. Azure OpenAI API error (HTTP %d): %s",
                    message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
