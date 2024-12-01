package org.mule.extension.vectors.internal.model.einstein;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Einstein AI's embedding model service that extends DimensionAwareEmbeddingModel.
 * This class handles the generation of text embeddings using Salesforce Einstein API.
 */
public class EinsteinEmbeddingModel extends DimensionAwareEmbeddingModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinEmbeddingModel.class);

  private static final String URL_BASE = "https://api.salesforce.com/einstein/platform/v1/models/";

  private final String modelName;
  private final Integer dimensions;
  private final String accessToken;

  /**
   * Private constructor used by the builder pattern.
   * Initializes the embedding model with Salesforce credentials and model configuration.
   *
   * @param einsteinModelConnection The Einstein connection
   * @param modelName Name of the Einstein embedding model to use
   * @param dimensions Number of dimensions for the embeddings
   */
  private EinsteinEmbeddingModel(EinsteinModelConnection einsteinModelConnection, String modelName, Integer dimensions) {
    // Default to SFDC text embedding model if none specified
    this.modelName = Utils.getOrDefault(modelName, Constants.EMBEDDING_MODEL_NAME_SFDC_TEXT_EMBEDDING_ADA_002);
    this.dimensions = dimensions;
    this.accessToken = getAccessToken(einsteinModelConnection);
  }

  /**
   * Returns the known dimension of the embedding model.
   *
   * @return The dimension size of the embeddings
   */
  protected Integer knownDimension() {
    return this.dimensions != null ? this.dimensions : EinsteinEmbeddingModelName.knownDimension(this.modelName());
  }

  /**
   * Returns the name of the current embedding model.
   *
   * @return The model name
   */
  public String modelName() {
    return this.modelName;
  }

  /**
   * Generates embeddings for a list of text segments.
   *
   * @param textSegments List of text segments to embed
   * @return Response containing list of embeddings and token usage information
   */
  public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
    // Convert TextSegments to plain strings
    List<String> texts = textSegments.stream().map(TextSegment::text).collect(Collectors.toList());
    return this.embedTexts(texts);
  }

  /**
   * Internal method to process text strings and generate embeddings.
   * Handles batching of requests to the Einstein API.
   *
   * @param texts List of text strings to embed
   * @return Response containing embeddings and token usage
   */
  private Response<List<Embedding>> embedTexts(List<String> texts) {
    List<Embedding> embeddings = new ArrayList<>();
    int tokenUsage = 0;

    // Process texts in batches of 16 (Einstein API limit)
    for(int x = 0; x < texts.size(); x += 16) {
      // Extract current batch
      List<String> batch = texts.subList(x, Math.min(x + 16, texts.size()));

      // Generate embeddings for current batch
      String response = generateEmbeddings(buildPayload(batch));
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

  /**
   * Authenticates with Salesforce and obtains an access token.
   *
   * @param einsteinModelConnection Salesforce einstein connection
   * @return Access token for API calls
   * @throws ModuleException if authentication fails
   */
  private String getAccessToken(EinsteinModelConnection einsteinModelConnection) {

    String urlString = EinsteinModelConnectionProvider.getOAuthURL(einsteinModelConnection.getSalesforceOrg());
    String params = EinsteinModelConnectionProvider.getOAuthParams(einsteinModelConnection.getClientId(), einsteinModelConnection.getClientSecret());

    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      // Configure connection for OAuth token request
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      // Write parameters to request body
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = params.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read and parse response
        try (java.io.BufferedReader br = new java.io.BufferedReader(
            new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
          StringBuilder response = new StringBuilder();
          String responseLine;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
          return new JSONObject(response.toString()).getString("access_token");
        }
      } else {
        throw new ModuleException(
            "Error while getting access token for \"EINSTEIN\" embedding model service. Response code: " + responseCode,
            MuleVectorsErrorType.AI_SERVICES_FAILURE);
      }
    } catch (Exception e) {
      throw new ModuleException(
          "Error while getting access token for \"EINSTEIN\" embedding model service.",
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }

  /**
   * Creates and configures an HTTP connection for Einstein API requests.
   *
   * @param url The endpoint URL
   * @param accessToken OAuth access token
   * @return Configured HttpURLConnection
   * @throws IOException if connection setup fails
   */
  private HttpURLConnection getConnectionObject(URL url, String accessToken) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
    conn.setRequestProperty("x-sfdc-app-context", "EinsteinGPT");
    conn.setRequestProperty("x-client-feature-id", "ai-platform-models-connected-app");
    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
    return conn;
  }

  /**
   * Builds JSON payload for single text embedding request.
   *
   * @param text Text to embed
   * @return JSON string payload
   */
  private static String buildPayload(String text) {
    JSONArray input = new JSONArray();
    input.put(text);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("input", input);
    return jsonObject.toString();
  }

  /**
   * Builds JSON payload for batch text embedding request.
   *
   * @param texts List of texts to embed
   * @return JSON string payload
   */
  private static String buildPayload(List<String> texts) {
    JSONArray input = new JSONArray(texts);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("input", input);
    return jsonObject.toString();
  }

  /**
   * Makes the API call to Einstein to generate embeddings.
   *
   * @param payload JSON payload for the request
   * @return JSON response string
   * @throws ModuleException if the API call fails
   */
  private String generateEmbeddings(String payload) {
    try {
      // Prepare connection
      String urlString = URL_BASE + this.modelName + "/embeddings";
      HttpURLConnection connection;
      try {
        URL url = new URL(urlString);
        connection = getConnectionObject(url, this.accessToken);
      } catch (Exception e) {
        throw new ModuleException(
            "Error while connecting to  \"EINSTEIN\" embedding model service.",
            MuleVectorsErrorType.AI_SERVICES_FAILURE,
            e);
      }

      // Send request
      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = payload.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read response
        try (java.io.BufferedReader br = new java.io.BufferedReader(
            new java.io.InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
          StringBuilder response = new StringBuilder();
          String responseLine;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
          return response.toString();
        }
      } else {
        throw new ModuleException(
            "Error while generating embeddings with \"EINSTEIN\" embedding model service. Response code: " + responseCode,
            MuleVectorsErrorType.AI_SERVICES_FAILURE);
      }
    } catch (ModuleException e) {
      throw e;
    } catch (Exception e) {
      throw new ModuleException(
          "Error while generating embeddings with \"EINSTEIN\" embedding model service.",
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }

  /**
   * Creates a new builder instance for EinsteinEmbeddingModel.
   *
   * @return A new builder instance
   */
  public static EinsteinEmbeddingModel.EinsteinEmbeddingModelBuilder builder() {
    return new EinsteinEmbeddingModel.EinsteinEmbeddingModelBuilder();
  }

  /**
   * Builder class for EinsteinEmbeddingModel.
   * Implements the Builder pattern for constructing EinsteinEmbeddingModel instances.
   */
  public static class EinsteinEmbeddingModelBuilder {
    private EinsteinModelConnection modelConnection;
    private String modelName;
    private Integer dimensions;
    private Tokenizer tokenizer;
    private HttpURLConnection connection;

    public EinsteinEmbeddingModelBuilder() {
    }

    public EinsteinEmbeddingModel.EinsteinEmbeddingModelBuilder modelConnection(EinsteinModelConnection modelConnection) {
      this.modelConnection = modelConnection;
      return this;
    }

    public EinsteinEmbeddingModel.EinsteinEmbeddingModelBuilder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public EinsteinEmbeddingModel.EinsteinEmbeddingModelBuilder modelName(EinsteinEmbeddingModelName modelName) {
      this.modelName = modelName.toString();
      return this;
    }

    public EinsteinEmbeddingModel.EinsteinEmbeddingModelBuilder dimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    /**
     * Builds and returns a new EinsteinEmbeddingModel instance.
     *
     * @return A new EinsteinEmbeddingModel configured with the builder's parameters
     */
    public EinsteinEmbeddingModel build() {
      return new EinsteinEmbeddingModel(this.modelConnection, this.modelName, this.dimensions);
    }
  }
}
