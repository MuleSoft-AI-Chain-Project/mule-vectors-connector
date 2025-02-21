package org.mule.extension.vectors.internal.model.multimodal.vertexai;

import com.google.cloud.aiplatform.v1beta1.EndpointName;
import com.google.cloud.aiplatform.v1beta1.PredictResponse;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceClient;
import com.google.gson.JsonObject;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.langchain4j.internal.RetryUtils.withRetry;

/**
 * Implementation of the EmbeddingMultimodalModel interface for Vertex AI.
 */
public class VertexAiEmbeddingMultimodalModel implements EmbeddingMultimodalModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(VertexAiEmbeddingMultimodalModel.class);

  private static final String DEFAULT_LOCATION = "us-central1";

  private static final String TEXT_EMBEDDING_FIELD_NAME = "textEmbedding";
  private static final String IMAGE_EMBEDDING_FIELD_NAME = "imageEmbedding";
  private static final String VIDEO_EMBEDDING_FIELD_NAME = "videoEmbedding";

  private final PredictionServiceClient predictionServiceClient;
  private final EndpointName endpointName;
  private final Integer maxRetries;

  /**
   * Constructs a new VertexAiEmbeddingMultimodalModel instance.
   *
   * @param predictionServiceClient The PredictionServiceClient used for predictions.
   * @param project                 The Google Cloud project ID.
   * @param location                The location of the model.
   * @param publisher               The publisher of the model.
   * @param modelName               The name of the model.
   * @param maxRetries              The maximum number of retry attempts for predictions. Defaults to 3 if null.
   */
  public VertexAiEmbeddingMultimodalModel(PredictionServiceClient predictionServiceClient,
                                          String project,
                                          String location,
                                          String publisher,
                                          String modelName,
                                          Integer maxRetries) {

    if(location == null || location.isEmpty()) location = DEFAULT_LOCATION;
    this.endpointName = EndpointName.ofProjectLocationPublisherModelName(project, location, publisher, modelName);
    this.predictionServiceClient = predictionServiceClient;
    this.maxRetries = maxRetries != null ? maxRetries : 3;
  }

  @Override
  public Integer dimension() {
    return VertexAiEmbeddingMultimodalModelName.knownDimension(this.endpointName.getModel());
  }

  @Override
  public Response<Embedding> embedText(String text) {
    return embedSingle(new VertexAiMultimodalInput(text, null));
  }

  @Override
  public Response<Embedding> embedImage(byte[] imageBytes) {
    return embedSingle(new VertexAiMultimodalInput(null, imageBytes));
  }

  @Override
  public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
    return embedSingle(new VertexAiMultimodalInput(text, imageBytes));
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<String> texts) {
    List<VertexAiMultimodalInput> inputs = texts.stream()
        .map(text -> new VertexAiMultimodalInput(text, null))
        .collect(Collectors.toList());
    return embedBatch(inputs);
  }

  @Override
  public Response<List<Embedding>> embedImages(List<byte[]> imageBytesList) {
    List<VertexAiMultimodalInput> inputs = imageBytesList.stream()
        .map(bytes -> new VertexAiMultimodalInput(null, bytes))
        .collect(Collectors.toList());
    return embedBatch(inputs);
  }

  private Response<Embedding> embedSingle(VertexAiMultimodalInput input) {
    try {
      // Log input details for debugging
      LOGGER.debug("Multimodal Input - Text Length: {}, Image Bytes: {}",
                   input.getText() != null ? input.getText().length() : "null",
                   input.getImage() != null ? input.getImage().length : "null");

      List<Value> instances = new ArrayList<>();
      instances.add(input.instanceValue());

      Value parameterValue = Value.newBuilder().build();

      // Validate input size before making API call
      validateInputSize(input);

      PredictResponse response = withRetry(() ->
           predictionServiceClient.predict(endpointName, instances, parameterValue), maxRetries);

      // LOGGER.debug("Predict Response " + response);

      Embedding embedding = response.getPredictionsList().stream()
          .map(VertexAiEmbeddingMultimodalModel::toEmbedding)
          .findFirst()
          .orElseThrow(() -> new RuntimeException("No embedding found in response"));

      return Response.from(embedding);

    } catch (Exception e) {
      LOGGER.error("Embedding generation failed", e);
      throw new RuntimeException("Error during embedding generation", e);
    }
  }

  private void validateInputSize(VertexAiMultimodalInput input) {

    int MAX_IMAGE_SIZE = 20 * 1024 * 1024;  // 5MB

    if (input.getImage() != null && input.getImage().length > MAX_IMAGE_SIZE) {
      throw new IllegalArgumentException("Image input exceeds maximum allowed size");
    }
  }

  private Response<List<Embedding>> embedBatch(List<VertexAiMultimodalInput> inputs) {
    try {
      List<Value> instances = inputs.stream()
          .map(input -> { return input.instanceValue(); })
          .collect(Collectors.toList());

      Value parameterValue = Value.newBuilder().build(); // Adjust if additional parameters are needed.

      PredictResponse response = withRetry(() -> predictionServiceClient.predict(endpointName, instances, parameterValue), maxRetries);

      //LOGGER.debug("Predict Response " + response);

      List<Embedding> embeddings = response.getPredictionsList().stream()
          .map(VertexAiEmbeddingMultimodalModel::toEmbedding)
          .collect(Collectors.toList());

      return Response.from(embeddings);
    } catch (Exception e) {
      throw new RuntimeException("Error during batch embedding generation", e);
    }
  }

  private static Embedding toEmbedding(Value prediction) {

    List<Float> vector = null;

    if (prediction.getStructValue().containsFields(TEXT_EMBEDDING_FIELD_NAME)) {

      LOGGER.debug ("Processing textEmbedding");
      Value textEmbedding = prediction.getStructValue().getFieldsOrThrow(TEXT_EMBEDDING_FIELD_NAME);
      float[] textVector = toVector(textEmbedding);
      vector = IntStream.range(0, textVector.length)
          .mapToObj(i -> textVector[i])
          .collect(Collectors.toList());
    }

    if (prediction.getStructValue().containsFields(IMAGE_EMBEDDING_FIELD_NAME)) {

      LOGGER.debug ("Processing imageEmbedding");
      Value imageEmbedding = prediction.getStructValue().getFieldsOrThrow(IMAGE_EMBEDDING_FIELD_NAME);
      float[] imageVector = toVector(imageEmbedding);
      vector = IntStream.range(0, imageVector.length)
          .mapToObj(i -> imageVector[i])
          .collect(Collectors.toList());
    }

    if (prediction.getStructValue().containsFields(VIDEO_EMBEDDING_FIELD_NAME)) {

      LOGGER.debug ("Processing videoEmbeddings");
      Value videoEmbeddings = prediction.getStructValue().getFieldsOrThrow(VIDEO_EMBEDDING_FIELD_NAME);
      if (videoEmbeddings.getListValue().getValues(0).getStructValue().containsFields("embedding")) {
        Value embeddings = videoEmbeddings.getListValue()
            .getValues(0)
            .getStructValue()
            .getFieldsOrThrow("embedding");
        float[] videoVector = toVector(embeddings);
        vector = IntStream.range(0, videoVector.length)
            .mapToObj(i -> videoVector[i])
            .collect(Collectors.toList());
      }
    }

    return Embedding.from(vector);
  }

  private static float[] toVector(Value value) {

    float[] floats = new float[value.getListValue().getValuesList().size()];
    int index = 0;
    for (Value v : value.getListValue().getValuesList()) {
      double d = v.getNumberValue();
      floats[index++] = Double.valueOf(d).floatValue();
    }
    return floats;
  }

  /**
   * Helper class for representing multimodal inputs.
   */
  private static class VertexAiMultimodalInput {
    private final String text;
    private final byte[] image;

    public VertexAiMultimodalInput(String text, byte[] image) {
      this.text = text;
      this.image = image;
    }

    public String getText() {
      return text;
    }

    public byte[] getImage() {
      return image;
    }

    public Value instanceValue() {

      Value instanceValue;

      try {

        Value.Builder instanceBuilder = Value.newBuilder();
        JsonObject jsonInstance = new JsonObject();

        if(text != null && !text.isEmpty()) {

          jsonInstance.addProperty("text", text);
        }

        if(image != null) {

          // Convert the image to Base64
          byte[] imageData = Base64.getEncoder().encode(image);
          String encodedImage = new String(imageData, StandardCharsets.UTF_8);
          JsonObject jsonImage = new JsonObject();
          jsonImage.addProperty("bytesBase64Encoded", encodedImage);
          jsonInstance.add("image", jsonImage);
        }

        Value.Builder builder = Value.newBuilder();
        JsonFormat.parser().merge(jsonInstance.toString(), builder);
        instanceValue =  builder.build();

      } catch (IOException e) {
        throw new RuntimeException("Error serializing input", e);
      }

      return instanceValue;
    }
  }

  public static class Builder {

    private PredictionServiceClient predictionServiceClient;
    private String projectId;
    private String location;
    private String publisher;
    private String modelName;
    private Integer maxRetries = 3;

    public Builder predictionServiceClient(PredictionServiceClient predictionServiceClient) {
      this.predictionServiceClient = predictionServiceClient;
      return this;
    }

    public Builder projectId(String projectId) {
      this.projectId = projectId;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder publisher(String publisher) {
      this.publisher = publisher;
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

    public VertexAiEmbeddingMultimodalModel build() {

      return new VertexAiEmbeddingMultimodalModel(predictionServiceClient, projectId, location, publisher, modelName, maxRetries);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
