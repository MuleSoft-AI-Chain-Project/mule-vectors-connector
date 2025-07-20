package org.mule.extension.vectors.internal.service.embeddings.mistralai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MistralAIService implements EmbeddingService {

  private MistralAIModelConnection mistralAIModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final int BATCH_SIZE = 16;
  private static final String EMBEDDINGS_ENDPOINT = "https://api.mistral.ai/v1/embeddings";

  public MistralAIService(MistralAIModelConnection mistralAIModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    this.mistralAIModelConnection = mistralAIModelConnection;
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
          byte[] body = buildEmbeddingsPayload(inputs, modelName);
          return HttpRequestHelper.executePostRequest(this.mistralAIModelConnection.getHttpClient(), EMBEDDINGS_ENDPOINT, buildAuthHeaders(), body, (int) this.mistralAIModelConnection.getTimeout())
                  .thenApply(this::handleEmbeddingResponse);
      } catch (JsonProcessingException e) {
          return CompletableFuture.failedFuture(new ModuleException("Failed to create request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
      }
  }

  private String handleEmbeddingResponse(HttpResponse response) {
    return HttpRequestHelper.handleEmbeddingResponse(response, "Mistral AI Error");
  }

 public byte[] buildEmbeddingsPayload(List<String> inputs, String modelName) throws JsonProcessingException {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", modelName);
      requestBody.put("input", inputs);
      return objectMapper.writeValueAsBytes(requestBody);
  }

  public Map<String, String> buildAuthHeaders() {
      Map<String, String> headers = new HashMap<>();
      headers.put("Authorization", "Bearer " + this.mistralAIModelConnection.getApiKey());
      headers.put("Content-Type", "application/json");
      headers.put("Accept", "application/json");
      return headers;
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
    List<String> texts = textSegments.stream()
          .map(TextSegment::text)
          .toList();
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


