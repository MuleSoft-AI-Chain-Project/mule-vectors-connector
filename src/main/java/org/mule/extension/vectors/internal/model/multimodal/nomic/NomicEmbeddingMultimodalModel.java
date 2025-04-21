package org.mule.extension.vectors.internal.model.multimodal.nomic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class NomicEmbeddingMultimodalModel implements EmbeddingMultimodalModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(NomicEmbeddingMultimodalModel.class);
  private final NomicModelConnection connection;
  private final String modelName;

  public NomicEmbeddingMultimodalModel(NomicModelConnection connection, String modelName) {
    this.connection = ValidationUtils.ensureNotNull(connection, "connection");
    this.modelName = (String)Utils.getOrDefault(modelName, "nomic-embed-vision-v1.5");
  }

  @Override
  public Integer dimension() {
    return NomicEmbeddingMultimodalModelName.knownDimension(this.modelName);
  }

  @Override
  public Response<Embedding> embedText(String text) {
    throw new ModuleException(String.format("Nomic %s model doesn't support generating embedding for text only", this.modelName), MuleVectorsErrorType.AI_SERVICES_FAILURE);
  }

  @Override
  public Response<Embedding> embedImage(byte[] imageBytes) {
    Response<List<Embedding>> response = embedImages(Arrays.asList(imageBytes));
    return Response.from(response.content().get(0), response.tokenUsage());
  }

  @Override
  public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
    LOGGER.warn(String.format("Nomic %s model doesn't support generating embedding for a combination of image and text. " +
        "The text will not be sent to the model to generate the embeddings.", this.modelName));
    return embedImage(imageBytes);
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {
    throw new ModuleException(String.format("Nomic %s model doesn't support generating embedding for text only.", this.modelName), MuleVectorsErrorType.AI_SERVICES_FAILURE);
  }

  @Override
  public Response<List<Embedding>> embedImages(List<byte[]> imageBytesList) {
    String responseJson = (String)connection.generateImageEmbeddings(imageBytesList, this.modelName);
    JSONObject response = new JSONObject(responseJson);
    JSONArray embeddings = response.getJSONArray("embeddings");
    JSONObject usage = response.getJSONObject("usage");
    
    List<Embedding> embeddingsList = new ArrayList<>();
    for (int j = 0; j < embeddings.length(); j++) {
        JSONArray embeddingArray = embeddings.getJSONArray(j);
        float[] embeddingValues = new float[embeddingArray.length()];
        for (int i = 0; i < embeddingArray.length(); i++) {
            embeddingValues[i] = (float)embeddingArray.getDouble(i);
        }
        embeddingsList.add(Embedding.from(embeddingValues));
    }

    TokenUsage tokenUsage = new TokenUsage(usage.getInt("total_tokens"), 0);
    return Response.from(embeddingsList, tokenUsage);
  }

  public static NomicEmbeddingMultimodalModelBuilder builder() {
    return new NomicEmbeddingMultimodalModelBuilder();
  }

  public static class NomicEmbeddingMultimodalModelBuilder {
    private NomicModelConnection connection;
    private String modelName;

    NomicEmbeddingMultimodalModelBuilder() {
    }

    public NomicEmbeddingMultimodalModelBuilder connection(NomicModelConnection connection) {
      this.connection = connection;
      return this;
    }

    public NomicEmbeddingMultimodalModelBuilder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public NomicEmbeddingMultimodalModel build() {
      return new NomicEmbeddingMultimodalModel(this.connection, this.modelName);
    }

    public String toString() {
      return "NomicEmbeddingMultimodalModel.NomicEmbeddingMultimodalModelBuilder(connection=" + this.connection + ", modelName=" + this.modelName + ")";
    }
  }
}
