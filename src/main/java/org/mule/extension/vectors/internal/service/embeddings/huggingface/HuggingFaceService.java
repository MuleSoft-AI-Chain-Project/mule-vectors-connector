package org.mule.extension.vectors.internal.service.embeddings.huggingface;

import org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

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
import org.json.JSONArray;

public class HuggingFaceService implements EmbeddingService {

  private HuggingFaceModelConnection huggingFaceModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;;
  private static final String INFERENCE_ENDPOINT = "https://router.huggingface.co/hf-inference/models/";
  private static final String PIPELINE_FEATURE_EXTRACTION_PATH = "/pipeline/feature-extraction";
  private final ObjectMapper objectMapper = new ObjectMapper();

  public HuggingFaceService(HuggingFaceModelConnection huggingFaceModelConnection,
                            EmbeddingModelParameters embeddingModelParameters) {
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

      return HttpRequestHelper.executePostRequest(this.huggingFaceModelConnection.getHttpClient(), url, headers, body,
                                                  (int) this.huggingFaceModelConnection.getTimeout())
          .thenApply(this::handleEmbeddingResponse);
    } catch (JsonProcessingException e) {
      return CompletableFuture.failedFuture(new ModuleException("Failed to create request body",
                                                                MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
    }
  }

  private String handleEmbeddingResponse(HttpResponse response) {
    return HttpRequestHelper.handleEmbeddingResponse(response, "Hugging face");
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

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {

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

