package org.mule.extension.vectors.internal.model.multimodal.nomic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// https://docs.nomic.ai/reference/api/embed-image-v-1-embedding-image-post
public class NomicEmbeddingMultimodalModel  implements EmbeddingMultimodalModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(NomicEmbeddingMultimodalModel.class);

  private static final String DEFAULT_BASE_URL = "https://api-atlas.nomic.ai/v1/";
  private static final Integer DEFAULT_MAX_RETRIES = 3;
  private final NomicClient client;
  private final String modelName;
  private final Integer maxRetries;

  public NomicEmbeddingMultimodalModel(String baseUrl, String apiKey, String modelName, Duration timeout, Integer maxRetries) {

    this.client = NomicClient.builder()
        .baseUrl((String) Utils.getOrDefault(baseUrl, DEFAULT_BASE_URL))
        .apiKey(ValidationUtils.ensureNotBlank(apiKey, "apiKey"))
        .timeout((Duration)Utils.getOrDefault(timeout, Duration.ofSeconds(60L)))
        .build();

    this.modelName = (String)Utils.getOrDefault(modelName, "nomic-embed-vision-v1.5");
    this.maxRetries = (Integer)Utils.getOrDefault(maxRetries, DEFAULT_MAX_RETRIES);
  }

  @Override
  public Integer dimension() {
    return NomicEmbeddingMultimodalModelName.knownDimension(this.modelName);
  }

  @Override
  public Response<Embedding> embedText(String text) {

    throw new ModuleException(String.format("Nomic %s model doesn't support generating embedding for text only", this.modelName) , MuleVectorsErrorType.AI_SERVICES_FAILURE);
  }

  @Override
  public Response<Embedding> embedImage(byte[] imageBytes) {

    NomicEmbeddingResponseBody response = client.embed(NomicImageEmbeddingRequestBody.builder()
                                                            .model(this.modelName)
                                                            .images(Arrays.asList(imageBytes))
                                                            .build());
    Embedding embedding = Embedding.from(response.getEmbeddings().get(0));
    TokenUsage tokenUsage = new TokenUsage(response.getUsage().getTotalTokens(), 0);
    return Response.from(embedding, tokenUsage);
  }

  @Override
  public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {

    LOGGER.warn(String.format("Nomic %s model doesn't support generating embedding for a combination of image and text. " +
                                  "The text will not be sent to the model to generate the embeddings.", this.modelName));
    return embedImage(imageBytes);
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {
    throw new ModuleException(String.format("Nomic %s model doesn't support generating embedding for text only.", this.modelName) , MuleVectorsErrorType.AI_SERVICES_FAILURE);
  }

  @Override
  public Response<List<Embedding>> embedImages(List<byte[]> imageBytesList) {

    NomicEmbeddingResponseBody response = client.embed(NomicImageEmbeddingRequestBody.builder()
                                                            .model(this.modelName)
                                                            .images(imageBytesList)
                                                            .build());
    List<Embedding> embeddings = (List)response.getEmbeddings().stream().map(Embedding::from).collect(Collectors.toList());
    TokenUsage tokenUsage = new TokenUsage(response.getUsage().getTotalTokens(), 0);
    return Response.from(embeddings, tokenUsage);
  }

  public static NomicEmbeddingMultimodalModelBuilder builder() {
    return new NomicEmbeddingMultimodalModelBuilder();
  }

  public static class NomicEmbeddingMultimodalModelBuilder {

    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Duration timeout;
    private Integer maxRetries;

    NomicEmbeddingMultimodalModelBuilder() {
    }

    public NomicEmbeddingMultimodalModelBuilder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public NomicEmbeddingMultimodalModelBuilder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public NomicEmbeddingMultimodalModelBuilder modelName(String modelName) {
      this.modelName = modelName;
      return this;
    }

    public NomicEmbeddingMultimodalModelBuilder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    public NomicEmbeddingMultimodalModelBuilder maxRetries(Integer maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public NomicEmbeddingMultimodalModel build() {
      return new NomicEmbeddingMultimodalModel(this.baseUrl, this.apiKey, this.modelName, this.timeout, this.maxRetries);
    }

    public String toString() {
      return "NomicEmbeddingMultimodalModel.NomicEmbeddingMultimodalModelBuilder(baseUrl=" + this.baseUrl + ", apiKey=" + this.apiKey + ", modelName=" + this.modelName + ", timeout=" + this.timeout + ", maxRetries=" + this.maxRetries + ")";
    }
  }
}
