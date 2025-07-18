package org.mule.extension.vectors.internal.service.embeddings.huggingface;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.azureopenai.AzureOpenAIService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HuggingFaceService implements EmbeddingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIService.class);
  private HuggingFaceModelConnection huggingFaceModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;;
  private static final String INFERENCE_ENDPOINT = "https://router.huggingface.co/hf-inference/models/";
  private static final String PIPELINE_FEATURE_EXTRACTION_PATH = "/pipeline/feature-extraction";
  private final ObjectMapper objectMapper = new ObjectMapper();

  public HuggingFaceService(HuggingFaceModelConnection huggingFaceModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    this.huggingFaceModelConnection = huggingFaceModelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
  }

  public Object generateTextEmbeddings(List<String> inputs, String modelName) {
    if (inputs == null || inputs.isEmpty()) {
      throw new IllegalArgumentException("Input list cannot be null or empty");
    }
    if (modelName == null || modelName.trim().isEmpty()) {
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
    String url = buildInferenceUrl(modelName);
    try {
      byte[] body = buildEmbeddingsPayload(inputs);
      Map<String, String> headers = buildAuthHeaders();
      headers.put("Content-Type", "application/json");

      return HttpRequestHelper.executePostRequest(this.huggingFaceModelConnection.getHttpClient(), url, headers, body, (int) this.huggingFaceModelConnection.getTimeout())
          .thenApply(this::handleEmbeddingResponse);
    } catch (JsonProcessingException e) {
      return CompletableFuture.failedFuture(new ModuleException("Failed to create request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
    }
  }

  private String handleEmbeddingResponse(HttpResponse response) {
    if (response.getStatusCode() != 200) {
      return handleErrorResponse(response, "Error generating embeddings");
    }
    try {
      return new String(response.getEntity().getBytes());
    } catch (IOException e) {
      throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }
  
  private String buildInferenceUrl(String modelName) {
    return INFERENCE_ENDPOINT + modelName + PIPELINE_FEATURE_EXTRACTION_PATH;
  }

  public byte[] buildEmbeddingsPayload(List<String> inputs) throws JsonProcessingException {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("inputs", inputs);
    return objectMapper.writeValueAsBytes(requestBody);
  }
  
  public Map<String, String> buildAuthHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + this.huggingFaceModelConnection.getApiKey());
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
    List<String> texts = textSegments.stream()
          .map(TextSegment::text)
          .toList();

    String result = (String) generateTextEmbeddings(texts, embeddingModelParameters.getEmbeddingModelName());
    {
        List<Embedding> embeddings = new ArrayList<>();
        JSONArray embeddingsArray = new JSONArray(result);
        
        for (int i = 0; i < embeddingsArray.length(); i++) {
            JSONArray embeddingArray = embeddingsArray.getJSONArray(i);
            float[] vector = new float[embeddingArray.length()];
            
            for (int j = 0; j < embeddingArray.length(); j++) {
            vector[j] = (float) embeddingArray.getDouble(j);
            }
            
            embeddings.add(Embedding.from(vector));
        }
        return Response.from(embeddings);
    }
  }
}

