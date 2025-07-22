package org.mule.extension.vectors.internal.service.embeddings.vertexai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class VertexAIService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIService.class);
    private VertexAIModelConnection vertexAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;

    // Vertex AI Constants
    private static final String VERTEX_AI_ENDPOINT_FORMAT = "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:predict";

    private static final String EMBEDDING_ERROR_MESSAGE = "Failed to generate embeddings";

    public VertexAIService(VertexAIModelConnection vertexAIModelConnection, EmbeddingModelParameters embeddingModelParameters) {
        this.vertexAIModelConnection = vertexAIModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
    }

    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        return executeEmbeddingRequest(buildTextPayload(inputs, modelName), modelName);
    }

    public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
        return executeEmbeddingRequest(buildImagePayload(imageBytesList), modelName);
    }
    
    private String executeEmbeddingRequest(String payload, String modelName) {
        try {
            return this.vertexAIModelConnection.getOrRefreshToken()
                    .thenCompose(token -> {
                        String url = String.format(VERTEX_AI_ENDPOINT_FORMAT, this.vertexAIModelConnection.getLocation(), this.vertexAIModelConnection.getProjectId(), this.vertexAIModelConnection.getLocation(), modelName);
                        Map<String, String> headers = Map.of("Authorization", "Bearer " + token, "Content-Type", "application/json");
                        return HttpRequestHelper.executePostRequest(this.vertexAIModelConnection.getHttpClient(), url, headers, payload.getBytes(StandardCharsets.UTF_8), (int) this.vertexAIModelConnection.getTotalTimeout());
                    })
                    .thenApply(this::handleEmbeddingResponse).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException(EMBEDDING_ERROR_MESSAGE, MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        if (response.getStatusCode() != 200) {
            return handleErrorResponse(response, EMBEDDING_ERROR_MESSAGE);
        }
        try {
            return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private String buildTextPayload(List<String> inputs, String modelName) {
        List<Map<String, String>> instances = inputs.stream()
                .map(input -> Map.of(modelName.startsWith("text") ? "content" : "text", input))
                .toList();
        return buildPayload(instances);
    }
    
    private String buildImagePayload(List<byte[]> imageBytesList) {
        List<Map<String, Map<String, String>>> instances = imageBytesList.stream()
                .map(imageBytes -> {
                    String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                    return Map.of("image", Map.of("bytesBase64Encoded", encodedImage));
                })
                .toList();
        return buildPayload(instances);
    }

    private String buildPayload(List<?> instances) {
        try {
            return this.vertexAIModelConnection.getObjectMapper().writeValueAsString(Map.of("instances", instances));
        } catch (JsonProcessingException e) {
            throw new ModuleException("Failed to build request payload", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e);
        }
    }

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
            String errorMsg = String.format("%s. Status: %d - %s", message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
         List<String> texts = textSegments.stream()
          .map(TextSegment::text)
          .toList();

      List<Embedding> allEmbeddings = new ArrayList<>();
      for (int i = 0; i < texts.size(); i += vertexAIModelConnection.getBatchSize()) {
        List<String> batch = texts.subList(i, Math.min(i + vertexAIModelConnection.getBatchSize(), texts.size()));
        String result = (String) generateTextEmbeddings(batch, embeddingModelParameters.getEmbeddingModelName());
        try {
            JSONObject response = new JSONObject(result);
            JSONArray predictions = response.getJSONArray("predictions");
            List<Embedding> embeddings = new ArrayList<>();
            
            for (int p = 0; p < predictions.length(); p++) {
                JSONObject prediction = predictions.getJSONObject(p);
                List<Float> vector = new ArrayList<>();
                
                JSONObject embeddingsObj = prediction.getJSONObject("embeddings");
                JSONArray values = embeddingsObj.getJSONArray("values");
                
                for (int j = 0; j < values.length(); j++) {
                    vector.add((float) values.getDouble(j));
                }
                embeddings.add(Embedding.from(vector));
            }
            allEmbeddings.addAll(embeddings);
        } catch (Exception e) {
            throw new ModuleException(EMBEDDING_ERROR_MESSAGE, MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
      }
      return Response.from(allEmbeddings);
    }
}

