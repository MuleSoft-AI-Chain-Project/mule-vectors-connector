/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.embeddings.ollama;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Alias("ollama")
@DisplayName("Ollama")
public class OllamaModelConnection implements BaseModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaModelConnection.class);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final long timeout;
    private static final String MODELS_ENDPOINT = "/api/tags";

    public OllamaModelConnection(String baseUrl, long timeout, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.httpClient = httpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getTimeout() {
        return timeout;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
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

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes());
            String errorMsg = String.format("%s. Status: %d - %s", message, response.getStatusCode(), errorBody);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
