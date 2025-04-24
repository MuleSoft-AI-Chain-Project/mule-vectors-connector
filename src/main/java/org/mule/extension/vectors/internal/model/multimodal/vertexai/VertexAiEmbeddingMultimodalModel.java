package org.mule.extension.vectors.internal.model.multimodal.vertexai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the EmbeddingMultimodalModel interface for Vertex AI.
 */
public class VertexAiEmbeddingMultimodalModel implements EmbeddingMultimodalModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(VertexAiEmbeddingMultimodalModel.class);

  private static final String TEXT_EMBEDDING_FIELD_NAME = "textEmbedding";
  private static final String IMAGE_EMBEDDING_FIELD_NAME = "imageEmbedding";

  private final VertexAIModelConnection connection;
  private final String modelName;

  /**
   * Constructs a new VertexAiEmbeddingMultimodalModel instance.
   *
   * @param connection The VertexAIModelConnection to use for API calls
   * @param modelName The name of the model
   */
  public VertexAiEmbeddingMultimodalModel(VertexAIModelConnection connection, String modelName) {
    this.connection = connection;
    this.modelName = modelName;
  }

  @Override
  public Integer dimension() {
    return VertexAiEmbeddingMultimodalModelName.knownDimension(modelName);
  }

  @Override
  public Response<Embedding> embedText(String text) {
    try {
      List<String> inputs = List.of(text);
      String result = (String) connection.generateTextEmbeddings(inputs, modelName);
      return Response.from(parseEmbeddings(result).get(0));
    } catch (Exception e) {
      LOGGER.error("Error embedding text", e);
      throw new RuntimeException("Error during text embedding generation", e);
    }
  }

  @Override
  public Response<Embedding> embedImage(byte[] imageBytes) {
    try {
      List<byte[]> inputs = List.of(imageBytes);
      String result = (String) connection.generateImageEmbeddings(inputs, modelName);
      return Response.from(parseEmbeddings(result).get(0));
    } catch (Exception e) {
      LOGGER.error("Error embedding image", e);
      throw new RuntimeException("Error during image embedding generation", e);
    }
  }

  @Override
  public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
    try {
      List<String> texts = List.of(text);
      List<byte[]> imageBytesList = List.of(imageBytes);
      String result = (String) connection.generateMultimodalEmbeddings(texts, imageBytesList, modelName);
      return Response.from(parseEmbeddings(result).get(0));
    } catch (Exception e) {
      LOGGER.error("Error embedding text and image", e);
      throw new RuntimeException("Error during text and image embedding generation", e);
    }
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {
    try {
      String result = (String) connection.generateTextEmbeddings(texts, modelName);
      return Response.from(parseEmbeddings(result));
    } catch (Exception e) {
      LOGGER.error("Error embedding texts", e);
      throw new RuntimeException("Error during texts embedding generation", e);
    }
  }

  @Override
  public Response<List<Embedding>> embedImages(List<byte[]> imageBytesList) {
    try {
      String result = (String) connection.generateImageEmbeddings(imageBytesList, modelName);
      return Response.from(parseEmbeddings(result));
    } catch (Exception e) {
      LOGGER.error("Error embedding images", e);
      throw new RuntimeException("Error during images embedding generation", e);
    }
  }

  private List<Embedding> parseEmbeddings(String jsonResponse) {
    try {
      JSONObject response = new JSONObject(jsonResponse);
      JSONArray predictions = response.getJSONArray("predictions");
      List<Embedding> embeddings = new ArrayList<>();
      
      for (int i = 0; i < predictions.length(); i++) {
        JSONObject prediction = predictions.getJSONObject(i);
        List<Float> vector = new ArrayList<>();
        
        if (prediction.has(IMAGE_EMBEDDING_FIELD_NAME)) {

          JSONArray imageEmbedding = prediction.getJSONArray(IMAGE_EMBEDDING_FIELD_NAME);
          for (int j = 0; j < imageEmbedding.length(); j++) {
            vector.add((float) imageEmbedding.getDouble(j));
          }

        // TODO: Change behavior, today text embedding is ignored if image embedding is present
        } else if (prediction.has(TEXT_EMBEDDING_FIELD_NAME)) {

          JSONArray textEmbedding = prediction.getJSONArray(TEXT_EMBEDDING_FIELD_NAME);
          for (int j = 0; j < textEmbedding.length(); j++) {
            vector.add((float) textEmbedding.getDouble(j));
          }
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

    public VertexAiEmbeddingMultimodalModel build() {
      return new VertexAiEmbeddingMultimodalModel(connection, modelName);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
