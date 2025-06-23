package org.mule.extension.vectors.internal.connection.model.azureaivision;

import org.json.JSONObject;

import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseImageModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class AzureAIVisionModelConnection implements BaseTextModelConnection, BaseImageModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureAIVisionModelConnection.class);

  private final String endpoint;
  private final String apiKey;
  private final String apiVersion;
  private final long timeout;
  private final HttpClient httpClient;

  public AzureAIVisionModelConnection(String endpoint, String apiKey, String apiVersion, long timeout, HttpClient httpClient) {
    this.endpoint = endpoint;
    this.apiKey = apiKey;
    this.apiVersion = apiVersion;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION;
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public void validate() {
    try {
      validateCredentials();
      } catch (IOException e) {
        throw new ModuleException("Failed to validate connection to Azure AI Vision", MuleVectorsErrorType.INVALID_CONNECTION, e);

      } catch (TimeoutException e) {
        throw new ModuleException("Failed to validate connection to Azure AI Vision", MuleVectorsErrorType.INVALID_CONNECTION, e);
      }


  }

  @Override
  public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
    LOGGER.debug(String.format("Embedding images, Model name: %s", modelName));
    try {
      if (imageBytesList.size() != 1) {
        throw new UnsupportedOperationException("Azure AI Vision only supports embedding one image at a time");
      }

      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("POST")
          .uri(endpoint + "/computervision/retrieval:vectorizeImage")
          .addQueryParam("api-version", apiVersion)
          .addQueryParam("model-version", modelName)
          .addHeader("Content-Type", "application/octet-stream")
          .addHeader("Ocp-Apim-Subscription-Key", apiKey)
          .entity(new ByteArrayHttpEntity(imageBytesList.get(0)));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);
      validateResponse(response);

      return new String(response.getEntity().getBytes());
    } catch (Exception e) {
      throw new RuntimeException("Failed to embed image", e);
    }
  }

  @Override
  public Object generateTextEmbeddings(List<String> inputs, String modelName) {
    LOGGER.debug(String.format("Embedding texts, Model name: %s", modelName));
    try {
      if (inputs.size() != 1) {
        throw new UnsupportedOperationException("Azure AI Vision only supports embedding one text at a time");
      }

      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("POST")
          .uri(endpoint + "/computervision/retrieval:vectorizeText")
          .addQueryParam("api-version", apiVersion)
          .addQueryParam("model-version", modelName)
          .addHeader("Content-Type", "application/json")
          .addHeader("Ocp-Apim-Subscription-Key", apiKey);

      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put("text", inputs.get(0));
      String jsonBody = jsonRequest.toString();
      requestBuilder.entity(new ByteArrayHttpEntity(jsonBody.getBytes()));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);
      validateResponse(response);

      return new String(response.getEntity().getBytes());
    } catch (Exception e) {
      throw new RuntimeException("Failed to embed text", e);
    }
  }

  private void validateCredentials() throws IOException, TimeoutException {
    HttpRequestBuilder requestBuilder = HttpRequest.builder()
        .method("POST")
        .uri(endpoint + "/computervision/retrieval:vectorizeImage")
        .addQueryParam("api-version", apiVersion)
        .addQueryParam("model-version", "")
        .addHeader("Ocp-Apim-Subscription-Key", apiKey);

    HttpRequestOptions options = HttpRequestOptions.builder()
        .responseTimeout((int)timeout)  // Cast long to int
        .followsRedirect(false)
        .build();

    HttpResponse response = httpClient.send(requestBuilder.build(), options);

    // We receive a bad request due to empty model if credentials are ok
    if (response.getStatusCode() != 400) {
      String errorBody = new String(response.getEntity().getBytes());
      throw new RuntimeException(String.format("Azure AI Vision API error (HTTP %d): %s",
                                               response.getStatusCode(), errorBody));
    }
  }

  private void validateResponse(HttpResponse response) {
    try {

      if (response.getStatusCode() != 200) {
        String errorBody = new String(response.getEntity().getBytes());
        throw new RuntimeException(String.format("Azure AI Vision API error (HTTP %d): %s", 
            response.getStatusCode(), errorBody));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read error response", e);
    }
  }
}
