package org.mule.extension.vectors.internal.service.embeddings.azureopenai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AzureOpenAIService implements EmbeddingService {

  private AzureOpenAIModelConnection azureOpenAIModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;
  private static final int BATCH_SIZE = 16;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AzureOpenAIService(AzureOpenAIModelConnection azureOpenAIModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    this.azureOpenAIModelConnection = azureOpenAIModelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
  }

  public Object generateTextEmbeddings(List<String> inputs, String deploymentName) {
      if (inputs == null || inputs.isEmpty()) {
          throw new IllegalArgumentException("Input list cannot be null or empty");
      }
      if (deploymentName == null || deploymentName.isEmpty()) {
          throw new IllegalArgumentException("Deployment name cannot be null or empty");
      }
      try {
          return generateTextEmbeddingsAsync(inputs, deploymentName).get();
      } catch (InterruptedException | ExecutionException e) {
          Thread.currentThread().interrupt();
          if (e.getCause() instanceof ModuleException) {
              throw (ModuleException) e.getCause();
          }
          throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
      }
  }

  private CompletableFuture<String> generateTextEmbeddingsAsync(List<String> inputs, String deploymentName) {
      String url = buildUrlForDeployment(deploymentName);
      try {
          byte[] body = buildTextEmbeddingPayload(inputs);
          return HttpRequestHelper.executePostRequest(this.azureOpenAIModelConnection.getHttpClient(), url, buildHeaders(), body, (int) this.azureOpenAIModelConnection.getTimeout())
                  .thenApply(this::handleEmbeddingResponse);
      } catch (JsonProcessingException e) {
          return CompletableFuture.failedFuture(new ModuleException("Failed to create request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
      }
  }

  private String handleEmbeddingResponse(HttpResponse response) {
    return HttpRequestHelper.handleEmbeddingResponse(response, "Azure AI Vision");
  }

  private String buildUrlForDeployment(String deploymentName) {
      try {
          String encodedDeployment = URLEncoder.encode(deploymentName, StandardCharsets.UTF_8.name());
          String encodedApiVersion = URLEncoder.encode(this.azureOpenAIModelConnection.getApiVersion(), StandardCharsets.UTF_8.name());
          return String.format("%s/openai/deployments/%s/embeddings?api-version=%s", this.azureOpenAIModelConnection.getEndpoint(), encodedDeployment, encodedApiVersion);
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
      headers.put("api-key", this.azureOpenAIModelConnection.getApiKey());
      headers.put("Content-Type", "application/json");
      return headers;
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
    List<String> texts = textSegments.stream().map(TextSegment::text).toList();
    {
      List<Embedding> embeddings = new ArrayList<>();
      int tokenUsage = 0;

      for(int x = 0; x < texts.size(); x += BATCH_SIZE) {
        // Extract current batch
        List<String> batch = texts.subList(x, Math.min(x + BATCH_SIZE, texts.size()));

        // Generate embeddings for current batch
        String response = (String) generateTextEmbeddings(batch, embeddingModelParameters.getEmbeddingModelName());
        JSONObject jsonResponse = new JSONObject(response);

        // Accumulate token usage
        tokenUsage += jsonResponse
            .getJSONObject("usage")
            .getInt("total_tokens");

        // Parse embeddings from response
        JSONArray embeddingsArray = jsonResponse.getJSONArray("data");

        // Process each embedding in the response
        for (int i = 0; i < embeddingsArray.length(); i++) {
          JSONObject embeddingObject = embeddingsArray.getJSONObject(i);
          JSONArray embeddingArray = embeddingObject.getJSONArray("embedding");

          // Convert JSON array to float array
          float[] vector = new float[embeddingArray.length()];
          for (int y = 0; y < embeddingArray.length(); y++) {
            vector[y] = (float) embeddingArray.getDouble(y);
          }

          embeddings.add(Embedding.from(vector));
        }
      }

      return Response.from(embeddings, new TokenUsage(tokenUsage));
    }
  }
}
