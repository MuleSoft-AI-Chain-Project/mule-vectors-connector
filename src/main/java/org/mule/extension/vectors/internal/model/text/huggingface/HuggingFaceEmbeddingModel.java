package org.mule.extension.vectors.internal.model.text.huggingface;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HuggingFaceEmbeddingModel extends DimensionAwareEmbeddingModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuggingFaceEmbeddingModel.class);

  private final HuggingFaceModelConnection connection;
  private final String modelName;

  private HuggingFaceEmbeddingModel(HuggingFaceModelConnection connection, String modelName) {
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

  private List<Embedding> parseEmbeddings(String jsonResponse) {
    try {
      List<Embedding> embeddings = new ArrayList<>();
      JSONArray embeddingsArray = new JSONArray(jsonResponse);
      
      for (int i = 0; i < embeddingsArray.length(); i++) {
        JSONArray embeddingArray = embeddingsArray.getJSONArray(i);
        float[] vector = new float[embeddingArray.length()];
        
        for (int j = 0; j < embeddingArray.length(); j++) {
          vector[j] = (float) embeddingArray.getDouble(j);
        }
        
        embeddings.add(Embedding.from(vector));
      }
      
      return embeddings;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing embeddings response", e);
    }
  }

  protected Integer knownDimension() {
    // Hugging Face models have varying dimensions, so we can't know ahead of time
    return null;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private HuggingFaceModelConnection connection;
    private String modelName;

    public Builder connection(HuggingFaceModelConnection connection) {
      this.connection = connection;
      return this;
    }

    public Builder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public HuggingFaceEmbeddingModel build() {
      return new HuggingFaceEmbeddingModel(connection, modelName);
    }
  }
}