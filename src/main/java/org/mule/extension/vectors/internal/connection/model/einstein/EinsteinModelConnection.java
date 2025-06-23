/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.model.einstein;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.api.connection.ConnectionException;
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

public class EinsteinModelConnection implements BaseTextModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinModelConnection.class);

    private static final String URI_OAUTH_TOKEN = "/services/oauth2/token";
    private static final String PARAM_GRANT_TYPE = "grant_type";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String EINSTEIN_PLATFORM_MODELS_URL = "https://api.salesforce.com/einstein/platform/v1/models/";
    private static final int TIMEOUT = 30000;

    private final String salesforceOrg;
    private final String clientId;
    private final String clientSecret;
    private final HttpClient httpClient;
    private String accessToken;

    public EinsteinModelConnection(String salesforceOrg, String clientId, String clientSecret, HttpClient httpClient)
            throws ConnectionException {
        this.salesforceOrg = salesforceOrg;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = httpClient;
        this.accessToken = getAccessTokenBlocking();
    }

    @Override
    public String getEmbeddingModelService() {
        return Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN;
    }

    @Override
    public void disconnect() {
        // No explicit disconnect action needed
    }

    @Override
    public void validate() {
        try {
            isAccessTokenValid().get(); // Block for validation
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to validate connection to Einstein", MuleVectorsErrorType.INVALID_CONNECTION, e);
        }
    }

    private String getOAuthURL() {
        return "https://" + salesforceOrg + URI_OAUTH_TOKEN;
    }

    private byte[] getOAuthBody() {
        String oAuthParams = PARAM_GRANT_TYPE + "=" + GRANT_TYPE_CLIENT_CREDENTIALS
                + "&" + PARAM_CLIENT_ID + "=" + clientId
                + "&" + PARAM_CLIENT_SECRET + "=" + clientSecret;
        return oAuthParams.getBytes();
    }

    private String getAccessTokenBlocking() throws ConnectionException {
        try {
            return getAccessTokenAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException("Error while getting access token for Einstein", e);
        }
    }

    private CompletableFuture<String> getAccessTokenAsync() {
        String tokenUrl = getOAuthURL();
        byte[] body = getOAuthBody();
        Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");

        return HttpRequestHelper.executePostRequest(httpClient, tokenUrl, headers, body, TIMEOUT)
                .thenApply(this::handleResponseToGetToken);
    }

    private String handleResponseToGetToken(HttpResponse response) {
        try {
            validateResponse(response);
            String responseStr = new String(response.getEntity().getBytes());
            return new JSONObject(responseStr).getString("access_token");
        } catch (IOException e) {
            throw new ModuleException("Failed to parse access token response.", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<Void> isAccessTokenValid() {
        String urlString = "https://" + salesforceOrg + "/services/oauth2/userinfo";
        Map<String, String> headers = Map.of("Authorization", "Bearer " + this.accessToken);

        return HttpRequestHelper.executeGetRequest(httpClient, urlString, headers, TIMEOUT)
                .thenAccept(response -> {
                    if (response.getStatusCode() != 200) {
                        throw new ModuleException("Error while validating access token for Einstein.", MuleVectorsErrorType.INVALID_CONNECTION);
                    }
                });
    }

    private void validateResponse(HttpResponse response) throws IOException {
        if (response.getStatusCode() != 200) {
            String errorBody = new String(response.getEntity().getBytes());
            throw new IOException(String.format("API error (HTTP %d): %s",
                    response.getStatusCode(), errorBody));
        }
    }

    private String buildEmbeddingsPayload(List<String> texts) {
        JSONArray input = new JSONArray(texts);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("input", input);
        return jsonObject.toString();
    }

    private String buildEmbeddingRequestUrl(String modelName) {
        return EINSTEIN_PLATFORM_MODELS_URL + modelName + "/embeddings";
    }

    private Map<String, String> buildEmbeddingRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.accessToken);
        headers.put("x-sfdc-app-context", "EinsteinGPT");
        headers.put("x-client-feature-id", "ai-platform-models-connected-app");
        headers.put("Content-Type", "application/json;charset=utf-8");
        return headers;
    }

    @Override
    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        try {
            return generateEmbeddingsAsync(inputs, modelName).get(); // Block to get the final result
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Error while generating embeddings with Einstein.",
                    MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateEmbeddingsAsync(List<String> inputs, String modelName) {
        String payload = buildEmbeddingsPayload(inputs);
        String urlString = buildEmbeddingRequestUrl(modelName);
        Map<String, String> headers = buildEmbeddingRequestHeaders();

        return HttpRequestHelper.executePostRequest(httpClient, urlString, headers, payload.getBytes(), TIMEOUT)
                .thenCompose(response -> {
                    if (response.getStatusCode() == 200) {
                        try {
                            return CompletableFuture.completedFuture(new String(response.getEntity().getBytes()));
                        } catch (IOException e) {
                            return CompletableFuture.failedFuture(e);
                        }
                    } else if (response.getStatusCode() == 401) {
                        LOGGER.debug("Salesforce access token expired. Refreshing token.");
                        return getAccessTokenAsync().thenCompose(newAccessToken -> {
                            this.accessToken = newAccessToken;
                            return generateEmbeddingsAsync(inputs, modelName); // Retry with new token
                        });
                    } else {
                        return CompletableFuture.failedFuture(handleErrorResponse(response));
                    }
                });
    }

    private ModuleException handleErrorResponse(HttpResponse response) {
        try {
            String responseBody = new String(response.getEntity().getBytes());
            LOGGER.error("Error (HTTP {}): {}", response.getStatusCode(), responseBody);
            MuleVectorsErrorType errorType = response.getStatusCode() == 429 ?
                    MuleVectorsErrorType.AI_SERVICES_RATE_LIMITING_ERROR : MuleVectorsErrorType.AI_SERVICES_FAILURE;
            return new ModuleException(
                    String.format("Error with Einstein API. Response code: %s. Response: %s.",
                            response.getStatusCode(), responseBody),
                    errorType);
        } catch (IOException e) {
            return new ModuleException("Failed to read error response from Einstein API.", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
}
