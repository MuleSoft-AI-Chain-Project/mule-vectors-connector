package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AzureAIVisionEmbeddingMultimodalModel implements EmbeddingMultimodalModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureAIVisionEmbeddingMultimodalModel.class);

  private AzureAIVisionModelConnection connection;
  private final String modelName;
  private Integer dimension;
  
  public AzureAIVisionEmbeddingMultimodalModel(AzureAIVisionModelConnection connection,
                                               String modelName,
                                               Integer maxRetries) {

    this.connection = connection;
    this.modelName = modelName != null ? modelName : AzureAIVisionEmbeddingMultimodalModelName.getDefaultModelName();
  }

  @Override
  public Integer dimension() {

    return this.dimension != null ? this.dimension : AzureAIVisionEmbeddingMultimodalModelName.knownDimension(this.modelName);
  }

  @Override
  public Response<Embedding> embedText(String text) {

    Embedding embedding = Embedding.from(connection.embedText(text, this.modelName));
    return Response.from(embedding);
  }

  @Override
  public Response<Embedding> embedImage(byte[] imageBytes) {

    Embedding embedding = Embedding.from(connection.embedImage(imageBytes, this.modelName));
    return Response.from(embedding);
  }

  @Override
  public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {

    LOGGER.warn(String.format("Azure AI Vision %s model doesn't support generating embedding for a combination of image and text. " +
                                  "The text will not be sent to the model to generate the embeddings.", this.modelName));
    return embedImage(imageBytes);
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {

    throw new RuntimeException(String.format("Azure AI Vision %s model doesn't support generating embedding for multiple text instances.", this.modelName));
  }

  @Override
  public Response<List<Embedding>> embedImages(List<byte[]> imageBytesList) {

    throw new RuntimeException(String.format("Azure AI Vision %s model doesn't support generating embedding for multiple image instances.", this.modelName));
  }

  public static class Builder {

    private AzureAIVisionModelConnection connection;
    private String modelName;
    private Integer maxRetries;

    public Builder connection(AzureAIVisionModelConnection connection) {
      this.connection = connection;
      return this;
    }

    public Builder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public Builder maxRetries(Integer maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public AzureAIVisionEmbeddingMultimodalModel build() {

      return new AzureAIVisionEmbeddingMultimodalModel(connection, modelName, maxRetries);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
