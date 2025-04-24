package org.mule.extension.vectors.internal.connection.model.nomic;

import org.mule.extension.vectors.internal.connection.model.BaseImageModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.entity.multipart.MultipartHttpEntity;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Alias("nomic")
@DisplayName("Nomic")
public class NomicModelConnection implements BaseTextModelConnection, BaseImageModelConnection {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(NomicModelConnection.class);
  private static final String BASE_URL = "https://api-atlas.nomic.ai/v1/";
  private static final String TEXT_EMBEDDING_URL = BASE_URL + "embedding/text";
  private static final String IMAGE_EMBEDDING_URL = BASE_URL + "embedding/image";
  
  private final String apiKey;
  private final long timeout;
  private final HttpClient httpClient;
  
  public NomicModelConnection(String apiKey, long timeout, HttpClient httpClient) {
    this.apiKey = apiKey;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }
  
  public String getApiKey() {
    return apiKey;
  }
  
  public long getTimeout() {
    return timeout;
  }
  
  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_NOMIC;
  }
  
  @Override
  public void connect() throws ConnectionException {
    if (apiKey.compareTo("demo") != 0) {
      try {
        validateConnection();
      } catch (Exception e) {
        throw new ConnectionException("Failed to connect to Nomic.", e);
      }
    }
  }
  
  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }
  
  @Override
  public boolean isValid() {
    try {
      validateConnection();
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to validate connection to Nomic.", e);
      return false;
    }
  }
  
  private void validateConnection() throws ConnectionException {
    try {
      
      JSONObject requestBody = new JSONObject();
      
      requestBody.put("task_type", "search_document");
      requestBody.put("texts", new JSONArray());
      requestBody.put("max_tokens_per_text", 0);
      
      // Using empty array for text embeddings to validate connection without token usage
      HttpRequest request = HttpRequest.builder()
      .uri(TEXT_EMBEDDING_URL)
      .addHeader("Authorization", "Bearer " + apiKey)
      .addHeader("Content-Type", "application/json")
      .method("POST")
      .entity(new ByteArrayHttpEntity(requestBody.toString().getBytes()))
      .build();
      
      HttpRequestOptions requestOptions = HttpRequestOptions.builder()
      .responseTimeout((int)timeout)
      .build();
      
      HttpResponse response = httpClient.send(request, requestOptions);
      
      if (response.getStatusCode() != 200) {
        throw new ConnectionException("Failed to validate connection to Nomic: HTTP " + response.getStatusCode());
      }
    } catch (TimeoutException e) {
      throw new ConnectionException("Connection to Nomic timed out", e);
    } catch (Exception e) {
      throw new ConnectionException("Failed to validate connection to Nomic", e);
    }
  }
  
  @Override
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
      String fileName = "image_" + index + ".png";  // Make sure to provide a file name
      parts.add(new HttpPart(partName, fileName, imageBytes, "image/png", imageBytes.length));
      index++;
    }

    HttpRequest request = HttpRequest.builder()
      .uri(IMAGE_EMBEDDING_URL)
      .method("POST")
      .addHeader("Content-Type", "multipart/form-data")  // Ensure this is correct
      .addHeader("Authorization", "Bearer " + apiKey)
      .entity(new MultipartHttpEntity(parts))
      .build();

    HttpRequestOptions requestOptions = HttpRequestOptions.builder()
      .responseTimeout((int) timeout)
      .build();

    HttpResponse response = httpClient.send(request, requestOptions);

    if (response.getStatusCode() != 200) {
      // Log the error response for debugging
      String errorResponse = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
      LOGGER.error("Failed to generate image embeddings: HTTP {}. Error: {}", response.getStatusCode(), errorResponse);
      throw new RuntimeException("Failed to generate image embeddings: HTTP " + response.getStatusCode() + ". Response: " + errorResponse);
    }

    return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
  } catch (Exception e) {
    LOGGER.error("Exception while generating image embeddings", e);
    throw new RuntimeException("Failed to generate image embeddings", e);
  }
}

  
  @Override
  public Object generateTextEmbeddings(List<String> inputs, String modelName) {
    try {
      
      JSONObject requestBody = new JSONObject();
      JSONArray texts = new JSONArray();
      for (String text : inputs) {
        texts.put(text);
      }
      requestBody.put("model", modelName);
      requestBody.put("texts", texts);
      
      HttpRequest request = HttpRequest.builder()
      .uri(TEXT_EMBEDDING_URL)
      .addHeader("Authorization", "Bearer " + apiKey)
      .addHeader("Content-Type", "application/json")
      .method("POST")
      .entity(new ByteArrayHttpEntity(requestBody.toString().getBytes()))
      .build();
      
      HttpRequestOptions requestOptions = HttpRequestOptions.builder()
      .responseTimeout((int)timeout)
      .build();
      
      HttpResponse response = httpClient.send(request, requestOptions);
      
      if (response.getStatusCode() != 200) {
        throw new RuntimeException("Failed to generate text embeddings: HTTP " + response.getStatusCode());
      }
      
      return new String(response.getEntity().getBytes());
    } catch (Exception e) {
      LOGGER.error("Error generating text embeddings", e);
      throw new RuntimeException("Error generating text embeddings", e);
    }
  }
}
