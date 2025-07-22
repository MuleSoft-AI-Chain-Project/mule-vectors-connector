package org.mule.extension.vectors.internal.service.embeddings.einstein;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.embeddings.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.azureopenai.AzureOpenAIService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EinsteinService implements EmbeddingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIService.class);
  private EinsteinModelConnection einsteinModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;
  private static final int BATCH_SIZE = 16;
  private static final String EINSTEIN_PLATFORM_MODELS_URL = "https://api.salesforce.com/einstein/platform/v1/models/";

  public EinsteinService(EinsteinModelConnection einsteinModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    this.einsteinModelConnection = einsteinModelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
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
      headers.put("Authorization", "Bearer " + this.einsteinModelConnection.getAccessToken());
      headers.put("x-sfdc-app-context", "EinsteinGPT");
      headers.put("x-client-feature-id", "ai-platform-models-connected-app");
      headers.put("Content-Type", "application/json;charset=utf-8");
      return headers;
  }

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

      return HttpRequestHelper.executePostRequest(this.einsteinModelConnection.getHttpClient(), urlString, headers, payload.getBytes(), (int)this.einsteinModelConnection.getTimeout())
              .thenCompose(response -> {
                  if (response.getStatusCode() == 200) {
                      try {
                          return CompletableFuture.completedFuture(new String(response.getEntity().getBytes()));
                      } catch (IOException e) {
                          return CompletableFuture.failedFuture(e);
                      }
                  } else if (response.getStatusCode() == 401) {
                      LOGGER.debug("Salesforce access token expired. Refreshing token.");
                      return this.einsteinModelConnection.getAccessTokenAsync().thenCompose(newAccessToken -> {
                          this.einsteinModelConnection.setAccessToken(newAccessToken);
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

  @Override
  public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
    List<String> texts = textSegments.stream().map(TextSegment::text).toList();
    {
        List<Embedding> embeddings = new ArrayList<>();
        int tokenUsage = 0;

        // Process texts in batches of 16 (Einstein API limit)
        for(int x = 0; x < texts.size(); x += BATCH_SIZE) {
            // Extract current batch
            List<String> batch = texts.subList(x, Math.min(x + 16, texts.size()));

            // Generate embeddings for current batch
            String response = (String) generateTextEmbeddings(batch, embeddingModelParameters.getEmbeddingModelName());
            JSONObject jsonResponse = new JSONObject(response);

            // Accumulate token usage
            tokenUsage += jsonResponse.getJSONObject("parameters")
                .getJSONObject("usage")
                .getInt("total_tokens");

            // Parse embeddings from response
            JSONArray embeddingsArray = jsonResponse.getJSONArray("embeddings");

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

