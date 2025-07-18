package org.mule.extension.vectors.internal.service.embeddings.openai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenAIService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIService.class);
    private OpenAIModelConnection openAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private static final int BATCH_SIZE = 16;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMBEDDINGS_ENDPOINT = "https://api.openai.com/v1/embeddings";

    public OpenAIService(OpenAIModelConnection openAIModelConnection, EmbeddingModelParameters embeddingModelParameters) {
        this.openAIModelConnection = openAIModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
    }

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

            return HttpRequestHelper.executePostRequest(this.openAIModelConnection.getHttpClient(), EMBEDDINGS_ENDPOINT, headers, jsonBody, (int) this.openAIModelConnection.getTimeout())
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
    
   public byte[] buildEmbeddingsPayload(List<String> inputs, String modelName) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", inputs);
        requestBody.put("model", modelName);
        return objectMapper.writeValueAsBytes(requestBody);
    }

    public Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.openAIModelConnection.getApiKey());
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

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream().map(TextSegment::text).toList();
        {
            List<Embedding> embeddings = new ArrayList<>();
            int tokenUsage = 0;

            for (int x = 0; x < texts.size(); x += BATCH_SIZE) {
                List<String> batch = texts.subList(x, Math.min(x + BATCH_SIZE, texts.size()));
                try {
                    String responseText = (String) generateTextEmbeddings(batch, embeddingModelParameters.getEmbeddingModelName());
                    JSONObject jsonResponse = new JSONObject(responseText);

                    tokenUsage += jsonResponse.getJSONObject("usage").getInt("total_tokens");

                    JSONArray embeddingsArray = jsonResponse.getJSONArray("data");
                    for (int i = 0; i < embeddingsArray.length(); i++) {
                        JSONObject embeddingObject = embeddingsArray.getJSONObject(i);
                        JSONArray embeddingArray = embeddingObject.getJSONArray("embedding");

                        float[] vector = new float[embeddingArray.length()];
                        for (int y = 0; y < embeddingArray.length(); y++) {
                            vector[y] = (float) embeddingArray.getDouble(y);
                        }

                        embeddings.add(Embedding.from(vector));
                    }
                } catch (Exception e) {
                    LOGGER.error("Error generating embeddings", e);
                    throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
                }
            }

            return Response.from(embeddings, new TokenUsage(tokenUsage));
        }
    }

    @Override
    public Response<Embedding> embedImage(byte[] imageBytes) {
        return null;
    }

    @Override
    public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
        return null;
    }
}
