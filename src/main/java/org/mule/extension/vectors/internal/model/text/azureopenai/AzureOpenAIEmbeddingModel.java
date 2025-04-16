package org.mule.extension.vectors.internal.model.text.azureopenai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AzureOpenAI AI's embedding model service that extends DimensionAwareEmbeddingModel.
 * This class handles the generation of text embeddings using Salesforce AzureOpenAI API.
 */
public class AzureOpenAIEmbeddingModel extends DimensionAwareEmbeddingModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIEmbeddingModel.class);

  private static final int BATCH_SIZE = 16;

  private final String modelName;
  private final Integer dimensions;
  private final AzureOpenAIModelConnection AzureOpenAIModelConnection;

  /**
   * Private constructor used by the builder pattern.
   * Initializes the embedding model with Salesforce credentials and model configuration.
   *
   * @param AzureOpenAIModelConnection The Salesforce connection
   * @param modelName Name of the AzureOpenAI embedding model to use
   * @param dimensions Number of dimensions for the embeddings
   */
  private AzureOpenAIEmbeddingModel(AzureOpenAIModelConnection AzureOpenAIModelConnection, String modelName, Integer dimensions) {
    // Default to SFDC text embedding model if none specified
    this.modelName = Utils.getOrDefault(modelName, EmbeddingModelHelper.TextEmbeddingModelNames.SFDC_OPENAI_TEXT_EMBEDDING_ADA_002.getModelName());
    this.dimensions = dimensions;
    this.AzureOpenAIModelConnection = AzureOpenAIModelConnection;
  }

  /**
   * Returns the known dimension of the embedding model.
   *
   * @return The dimension size of the embeddings
   */
  protected Integer knownDimension() {
    return this.dimensions != null ? this.dimensions : AzureOpenAIEmbeddingModelName.knownDimension(this.modelName());
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
   * Handles batching of requests to the AzureOpenAI API.
   *
   * @param texts List of text strings to embed
   * @return Response containing embeddings and token usage
   */
  private Response<List<Embedding>> embedTexts(List<String> texts) {
    List<Embedding> embeddings = new ArrayList<>();
    int tokenUsage = 0;

    for(int x = 0; x < texts.size(); x += BATCH_SIZE) {
      // Extract current batch
      List<String> batch = texts.subList(x, Math.min(x + BATCH_SIZE, texts.size()));

      // Generate embeddings for current batch
      String response = (String)AzureOpenAIModelConnection.generateEmbeddings(batch, modelName);
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

  /**
   * Creates a new builder instance for AzureOpenAIEmbeddingModel.
   *
   * @return A new builder instance
   */
  public static AzureOpenAIEmbeddingModel.AzureOpenAIEmbeddingModelBuilder builder() {
    return new AzureOpenAIEmbeddingModel.AzureOpenAIEmbeddingModelBuilder();
  }

  /**
   * Builder class for AzureOpenAIEmbeddingModel.
   * Implements the Builder pattern for constructing AzureOpenAIEmbeddingModel instances.
   */
  public static class AzureOpenAIEmbeddingModelBuilder {
    private AzureOpenAIModelConnection modelConnection;
    private String modelName;
    private Integer dimensions;

    public AzureOpenAIEmbeddingModelBuilder() {
    }

    public AzureOpenAIEmbeddingModel.AzureOpenAIEmbeddingModelBuilder connection(AzureOpenAIModelConnection modelConnection) {
      this.modelConnection = modelConnection;
      return this;
    }

    public AzureOpenAIEmbeddingModel.AzureOpenAIEmbeddingModelBuilder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public AzureOpenAIEmbeddingModel.AzureOpenAIEmbeddingModelBuilder modelName(AzureOpenAIEmbeddingModelName modelName) {
      this.modelName = modelName.toString();
      return this;
    }

    public AzureOpenAIEmbeddingModel.AzureOpenAIEmbeddingModelBuilder dimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    /**
     * Builds and returns a new AzureOpenAIEmbeddingModel instance.
     *
     * @return A new AzureOpenAIEmbeddingModel configured with the builder's parameters
     */
    public AzureOpenAIEmbeddingModel build() {
      return new AzureOpenAIEmbeddingModel(this.modelConnection, this.modelName, this.dimensions);
    }
  }
}
