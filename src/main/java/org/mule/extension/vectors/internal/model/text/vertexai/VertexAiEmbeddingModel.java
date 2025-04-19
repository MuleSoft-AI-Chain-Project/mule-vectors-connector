package org.mule.extension.vectors.internal.model.text.vertexai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A model class for interacting with the Vertex AI Embedding service.
 * This model generates embeddings for text segments using Google's Vertex AI platform.
 */
public class VertexAiEmbeddingModel extends DimensionAwareEmbeddingModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(VertexAiEmbeddingModel.class);

  private final VertexAIModelConnection connection;
  private final String modelName;

  /**
   * Constructs a new VertexAiEmbeddingModel.
   *
   * @param connection The VertexAI model connection
   * @param modelName The name of the model to use
   */
  public VertexAiEmbeddingModel(VertexAIModelConnection connection, String modelName) {
    this.connection = connection;
    this.modelName = modelName;
  }

  @Override
  public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
    try {
      List<String> texts = segments.stream()
          .map(TextSegment::text)
          .collect(Collectors.toList());

      String result = (String) connection.generateTextEmbeddings(texts, modelName);
      return Response.from(parseEmbeddings(result));
    } catch (Exception e) {
      LOGGER.error("Error generating embeddings", e);
      throw new RuntimeException("Failed to generate embeddings", e);
    }
  }

  protected Integer knownDimension() {
    return VertexAiEmbeddingModelName.knownDimension(modelName);
  }

  private List<Embedding> parseEmbeddings(String jsonResponse) {
    try {
      JSONObject response = new JSONObject(jsonResponse);
      JSONArray predictions = response.getJSONArray("predictions");
      List<Embedding> embeddings = new ArrayList<>();
      
      for (int i = 0; i < predictions.length(); i++) {
        JSONObject prediction = predictions.getJSONObject(i);
        List<Float> vector = new ArrayList<>();
        
        JSONObject embeddingsObj = prediction.getJSONObject("embeddings");
        JSONArray values = embeddingsObj.getJSONArray("values");
        
        for (int j = 0; j < values.length(); j++) {
          vector.add((float) values.getDouble(j));
        }
        
        embeddings.add(Embedding.from(vector));
      }
      
      return embeddings;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing embeddings response", e);
    }
  }

  public static class Builder {
    private VertexAIModelConnection connection;
    private String modelName;

    public Builder connection(VertexAIModelConnection connection) {
      this.connection = connection; 
      return this;
    }

    public Builder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public VertexAiEmbeddingModel build() {
      return new VertexAiEmbeddingModel(connection, modelName);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
