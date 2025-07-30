package org.mule.extension.vectors.internal.service.embeddings.openai;

import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIService implements EmbeddingService {

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
    try {
      if (inputs == null || inputs.isEmpty()) {
        throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      if (modelName == null || modelName.isEmpty()) {
        throw new IllegalArgumentException("Model name cannot be null or empty");
      }
      return generateTextEmbeddingsAsync(inputs, modelName).get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }

  private CompletableFuture<String> generateTextEmbeddingsAsync(List<String> inputs, String modelName) {
    try {
      byte[] jsonBody = buildEmbeddingsPayload(inputs, modelName);
      Map<String, String> headers = buildAuthHeaders();
      headers.put("Content-Type", "application/json");

      return HttpRequestHelper.executePostRequest(this.openAIModelConnection.getHttpClient(), EMBEDDINGS_ENDPOINT, headers,
                                                  jsonBody, (int) this.openAIModelConnection.getTimeout())
          .thenApply(response -> {
            return HttpRequestHelper.handleEmbeddingResponse(response, "Open AI Error");
          });
    } catch (JsonProcessingException e) {
      return CompletableFuture.failedFuture(new ModuleException("Failed to create request body",
                                                                MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
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

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {
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
          throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
      }

      return Response.from(embeddings, new TokenUsage(tokenUsage));
    }
  }
}
