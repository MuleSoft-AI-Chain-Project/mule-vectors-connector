package org.mule.extension.vectors.internal.service.embeddings.nomic;

import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
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

public class NomicService implements EmbeddingService {

  private NomicModelConnection nomicModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String BASE_URL = "https://api-atlas.nomic.ai/v1/";
  private static final String TEXT_EMBEDDING_URL = BASE_URL + "embedding/text";
  private static final String IMAGE_EMBEDDING_URL = BASE_URL + "embedding/image";

  public NomicService(NomicModelConnection nomicModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    this.nomicModelConnection = nomicModelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
  }

  public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
    if (imageBytesList == null || imageBytesList.isEmpty()) {
      throw new IllegalArgumentException("No images provided for embedding.");
    }
    try {
      return generateImageEmbeddingsAsync(imageBytesList, modelName).get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      if (e.getCause() instanceof ModuleException) {
        throw (ModuleException) e.getCause();
      }
      throw new ModuleException("Failed to generate image embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }

  private CompletableFuture<String> generateImageEmbeddingsAsync(List<byte[]> imageBytesList, String modelName) {
    List<HttpPart> parts = buildImageMultipartPayload(imageBytesList, modelName);
    return HttpRequestHelper.executeMultipartPostRequest(this.nomicModelConnection.getHttpClient(), IMAGE_EMBEDDING_URL,
                                                         buildAuthHeaders(), parts, (int) this.nomicModelConnection.getTimeout())
        .thenApply(this::handleEmbeddingResponse);
  }

  public Object generateTextEmbeddings(List<String> inputs, String modelName) {
    try {
      return generateTextEmbeddingsAsync(inputs, modelName).get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      if (e.getCause() instanceof ModuleException) {
        throw (ModuleException) e.getCause();
      }
      throw new ModuleException("Failed to generate text embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }

  private CompletableFuture<String> generateTextEmbeddingsAsync(List<String> inputs, String modelName) {
    try {
      byte[] body = buildTextEmbeddingsPayload(inputs, modelName);
      return HttpRequestHelper.executePostRequest(this.nomicModelConnection.getHttpClient(), TEXT_EMBEDDING_URL,
                                                  buildAuthHeaders(), body, (int) this.nomicModelConnection.getTimeout())
          .thenApply(this::handleEmbeddingResponse);
    } catch (JsonProcessingException e) {
      return CompletableFuture.failedFuture(new ModuleException("Failed to create text embedding request body",
                                                                MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
    }
  }

  private String handleEmbeddingResponse(HttpResponse response) {
    return HttpRequestHelper.handleEmbeddingResponse(response, "Nomic  Error");
  }

  private List<HttpPart> buildImageMultipartPayload(List<byte[]> imageBytesList, String modelName) {
    List<HttpPart> parts = new ArrayList<>();
    byte[] modelBytes = modelName.getBytes(StandardCharsets.UTF_8);
    parts.add(new HttpPart("model", modelBytes, "text/plain", modelBytes.length));

    int index = 0;
    for (byte[] imageBytes : imageBytesList) {
      String fileName = "image_" + index++ + ".png";
      parts.add(new HttpPart("images", fileName, imageBytes, "image/png", imageBytes.length));
    }
    return parts;
  }

  private byte[] buildTextEmbeddingsPayload(List<String> inputs, String modelName) throws JsonProcessingException {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", modelName);
    requestBody.put("texts", inputs);
    return objectMapper.writeValueAsBytes(requestBody);
  }

  private Map<String, String> buildAuthHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + this.nomicModelConnection.getApiKey());
    return headers;
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {

    String responseJson = (String) generateTextEmbeddings(texts, embeddingModelParameters.getEmbeddingModelName());
    JSONObject response = new JSONObject(responseJson);
    JSONArray embeddings = response.getJSONArray("embeddings");
    JSONObject usage = response.getJSONObject("usage");

    List<Embedding> embeddingsList = new ArrayList<>();
    for (int j = 0; j < embeddings.length(); j++) {
      JSONArray embeddingArray = embeddings.getJSONArray(j);
      float[] embeddingValues = new float[embeddingArray.length()];
      for (int i = 0; i < embeddingArray.length(); i++) {
        embeddingValues[i] = (float) embeddingArray.getDouble(i);
      }
      embeddingsList.add(Embedding.from(embeddingValues));
    }

    TokenUsage tokenUsage = new TokenUsage(usage.getInt("total_tokens"), 0);
    return Response.from(embeddingsList, tokenUsage);
  }
}


