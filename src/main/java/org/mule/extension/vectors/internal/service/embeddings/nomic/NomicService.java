package org.mule.extension.vectors.internal.service.embeddings.nomic;

import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.entity.multipart.MultipartHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NomicService implements EmbeddingService {

  private NomicModelConnection nomicModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String BASE_URL = "https://api-atlas.nomic.ai/v1/";
  private static final String TEXT_EMBEDDING_URL = BASE_URL + "embedding/text";
  private static final String IMAGE_EMBEDDING_URL = BASE_URL + "embedding/image";
  private static final Logger LOGGER = LoggerFactory.getLogger(NomicService.class);

  public NomicService(NomicModelConnection nomicModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    this.nomicModelConnection = nomicModelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
  }

  public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
    try {
      if (imageBytesList == null || imageBytesList.isEmpty()) {
        throw new IllegalArgumentException("No images provided for embedding.");
      }

      List<HttpPart> parts = new ArrayList<>();

      // Model part - use charset correctly
      byte[] modelBytes = modelName.getBytes(StandardCharsets.UTF_8);
      parts.add(new HttpPart("model", modelBytes, "text/plain", modelBytes.length));

      // Image parts
      int index = 0;
      for (byte[] imageBytes : imageBytesList) {
        // Create file-like HttpPart for each image
        String partName = "images";
        String fileName = "image_" + index + ".png"; // Make sure to provide a file name
        parts.add(new HttpPart(partName, fileName, imageBytes, "image/png", imageBytes.length));
        index++;
      }

      HttpRequest request = HttpRequest.builder()
          .uri(IMAGE_EMBEDDING_URL)
          .method("POST")
          .addHeader("Content-Type", "multipart/form-data") // Ensure this is correct
          .addHeader("Authorization", "Bearer " + nomicModelConnection.getApiKey())
          .entity(new MultipartHttpEntity(parts))
          .build();

      HttpRequestOptions requestOptions = HttpRequestOptions.builder()
          .responseTimeout((int) nomicModelConnection.getTimeout())
          .build();

      HttpResponse response = nomicModelConnection.getHttpClient().send(request, requestOptions);

      if (response.getStatusCode() != 200) {
        // Log the error response for debugging
        String errorResponse = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        LOGGER.error("Failed to generate image embeddings: HTTP {}. Error: {}", response.getStatusCode(), errorResponse);
        throw new RuntimeException("Failed to generate image embeddings: HTTP " + response.getStatusCode() + ". Response: "
            + errorResponse);
      }

      return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      LOGGER.error("Exception while generating image embeddings", e);
      throw new RuntimeException("Failed to generate image embeddings", e);
    }
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
  public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
    LOGGER.warn(String.format("Nomic %s model doesn't support generating embedding for a combination of image and text. " +
        "The text will not be sent to the model to generate the embeddings.", embeddingModelParameters.getEmbeddingModelName()));
    return embedImage(imageBytes);
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

  @Override
  public Response<Embedding> embedImage(byte[] imageBytes) {
    try {
      String responseJson =
          (String) generateImageEmbeddings(List.of(imageBytes), embeddingModelParameters.getEmbeddingModelName());
      JSONObject response = new JSONObject(responseJson);
      JSONArray embeddings = response.getJSONArray("embeddings");

      if (embeddings.length() > 0) {
        JSONArray embeddingArray = embeddings.getJSONArray(0);
        float[] embeddingValues = new float[embeddingArray.length()];
        for (int i = 0; i < embeddingArray.length(); i++) {
          embeddingValues[i] = (float) embeddingArray.getDouble(i);
        }
        return Response.from(Embedding.from(embeddingValues));
      } else {
        throw new RuntimeException("No embedding generated for the image");
      }
    } catch (Exception e) {
      LOGGER.error("Failed to process image embedding response", e);
      throw new RuntimeException("Failed to process image embedding response", e);
    }
  }

  public Response<List<Embedding>> embedImages(List<byte[]> imageBytesList) {
    try {
      String responseJson = (String) generateImageEmbeddings(imageBytesList, embeddingModelParameters.getEmbeddingModelName());
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
    } catch (Exception e) {
      LOGGER.error("Failed to process images embedding response", e);
      throw new RuntimeException("Failed to process images embedding response", e);
    }
  }

}


