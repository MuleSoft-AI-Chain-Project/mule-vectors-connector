package org.mule.extension.vectors.internal.model.text.einstein;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Einstein AI's embedding model service that extends DimensionAwareEmbeddingModel.
 * This class handles the generation of text embeddings using Salesforce Einstein API.
 */
public class EinsteinEmbeddingModel extends DimensionAwareEmbeddingModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinEmbeddingModel.class);

  private final String modelName;
  private final Integer dimensions;
  private final EinsteinModelConnection einsteinModelConnection;

  /**
   * Private constructor used by the builder pattern.
   * Initializes the embedding model with Salesforce credentials and model configuration.
   *
   * @param einsteinModelConnection The Salesforce connection
   * @param modelName Name of the Einstein embedding model to use
   * @param dimensions Number of dimensions for the embeddings
   */
  private EinsteinEmbeddingModel(EinsteinModelConnection einsteinModelConnection, String modelName, Integer dimensions) {
    // Default to SFDC text embedding model if none specified
    this.modelName = Utils.getOrDefault(modelName, EmbeddingModelHelper.TextEmbeddingModelNames.SFDC_TEXT_EMBEDDING_ADA_002.getModelName());
    this.dimensions = dimensions;
    this.einsteinModelConnection = einsteinModelConnection;
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
      String response = einsteinModelConnection.generateEmbeddings(batch, modelName);
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

    public EinsteinEmbeddingModelBuilder() {
    }

    public EinsteinEmbeddingModel.EinsteinEmbeddingModelBuilder connection(EinsteinModelConnection modelConnection) {
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
