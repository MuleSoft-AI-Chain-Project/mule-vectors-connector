package org.mule.extension.vectors.internal.operation;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createEmbeddingResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import org.mule.extension.vectors.api.metadata.EmbeddingResponseAttributes;
import org.mule.extension.vectors.api.metadata.MultimodalEmbeddingResponseAttributes;
import org.mule.extension.vectors.api.metadata.TokenUsage;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.EmbeddingErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceFactoryBuilder;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Container for embedding operations, providing methods to generate embeddings from text or documents.
 */
public class EmbeddingOperations {

  /**
   * Generates embeddings from a given text string. The text can optionally be segmented before embedding.
   *
   * @param embeddingConfiguration the configuration for the embedding service.
   * @param modelConnection the connection to the embedding model.
   * @param inputs the input list of texts to generate embeddings from.
   * @param embeddingModelParameters parameters for the embedding model to be used.
   * @return a {@link org.mule.runtime.extension.api.runtime.operation.Result} containing the list of embeddings in JSON format and metadata.
   * @throws ModuleException if an error occurs during the embedding process.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-generate-from-text")
  @DisplayName("[Embedding] Generate from text")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/EmbeddingGenerateResponse.json")
  public Result<InputStream, EmbeddingResponseAttributes> generateEmbeddingFromText(@Config EmbeddingConfiguration embeddingConfiguration,
                                                                                    @Connection BaseModelConnection modelConnection,
                                                                                    @Alias("inputs") @DisplayName("Input Texts") @Content List<String> inputs,
                                                                                    @ParameterGroup(
                                                                                        name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {
      List<Embedding> embeddings;
      TokenUsage tokenUsage = null;
      int dimension = 0;
      Response<List<Embedding>> embeddingsResponse = new EmbeddingServiceFactoryBuilder(modelConnection)
          .getBuilder(modelConnection, embeddingModelParameters).build().embedTexts(inputs);
      embeddings = embeddingsResponse.content();
      if (embeddingsResponse.tokenUsage() != null) {
        var usage = embeddingsResponse.tokenUsage();
        tokenUsage = new TokenUsage(
                                    usage.inputTokenCount() != null ? usage.inputTokenCount() : 0,
                                    usage.outputTokenCount() != null ? usage.outputTokenCount() : 0,
                                    usage.totalTokenCount() != null ? usage.totalTokenCount() : 0);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      JsonGenerator generator = new JsonFactory().createGenerator(out);
      generator.writeStartObject();

      JSONObject jsonObject = new JSONObject();
      JSONArray jsonTextSegments = IntStream.range(0, inputs.size())
          .mapToObj(i -> {
            JSONObject jsonSegment = new JSONObject();
            jsonSegment.put(Constants.JSON_KEY_TEXT, inputs.get(i));
            JSONObject jsonMetadata = new JSONObject();
            jsonMetadata.put(Constants.JSON_KEY_INDEX, i);
            jsonSegment.put(Constants.JSON_KEY_METADATA, jsonMetadata);
            return jsonSegment;
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_TEXT_SEGMENTS, jsonTextSegments);

      JSONArray jsonEmbeddings = IntStream.range(0, embeddings.size())
          .mapToObj(i -> {
            return embeddings.get(i).vector();
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_EMBEDDINGS, jsonEmbeddings);
      dimension = embeddings.get(0).vector().length;
      jsonObject.put(Constants.JSON_KEY_DIMENSION, dimension);


      int finalDimension = dimension;
      HashMap<String, Object> attributes = new HashMap<>();
      attributes.put("embeddingModelName", embeddingModelParameters.getEmbeddingModelName());
      attributes.put("embeddingModelDimension", finalDimension);
      if (tokenUsage != null) {
        attributes.put("tokenUsage", tokenUsage);
      }
      return createEmbeddingResponse(IOUtils.toInputStream(jsonObject.toString(), StandardCharsets.UTF_8), attributes);

    } catch (Exception e) {

      throw new ModuleException(
                                String.format("Error while generating embedding from texts \"%s\"", inputs),
                                MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
                                e);
    }
  }

  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-generate-from-media")
  @DisplayName("[Embedding] Generate from media")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/EmbeddingGenerateResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, MultimodalEmbeddingResponseAttributes>
  generateEmbeddingFromMedia(@Config EmbeddingConfiguration embeddingConfiguration,
                                    @Connection BaseModelConnection modelConnection,
                                    @ParameterGroup(name = "Media") EmbeddingMediaBinaryParameters mediaBinaryParameters,
                                    @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {

      List<TextSegment> textSegments = new LinkedList<>();
      TokenUsage tokenUsage = null;
      int multimodalEmbeddingModelDimension = 0;
      JSONObject jsonObject = new JSONObject();

      JSONArray jsonEmbeddings = new JSONArray();

      // Convert InputStream to byte array
      byte[] mediaBytes = IOUtils.toByteArray(mediaBinaryParameters.getBinaryInputStream());

      if(mediaBinaryParameters.getMediaType().equals(MEDIA_TYPE_IMAGE)) {

        Response<Embedding> response = mediaBinaryParameters.getLabel() != null && !mediaBinaryParameters.getLabel().isEmpty() ?
            new EmbeddingServiceFactoryBuilder(modelConnection).getServiceProvider().getBuilder(modelConnection, embeddingModelParameters).build().embedTextAndImage(mediaBinaryParameters.getLabel(), mediaBytes) :
            new EmbeddingServiceFactoryBuilder(modelConnection).getServiceProvider().getBuilder(modelConnection, embeddingModelParameters).build().embedImage(mediaBytes);
        Embedding embedding = response.content();
        tokenUsage = response.tokenUsage() != null ?
            new TokenUsage(response.tokenUsage().inputTokenCount() != null ? response.tokenUsage().inputTokenCount() : 0,
                           response.tokenUsage().outputTokenCount() != null ? response.tokenUsage().outputTokenCount() : 0,
                           response.tokenUsage().totalTokenCount() != null ? response.tokenUsage().totalTokenCount(): 0)
            : null;
        jsonEmbeddings.put(embedding.vector());
        multimodalEmbeddingModelDimension = embedding.dimension();
      } else {

        throw new ModuleException(
            String.format("Media type %s not supported.", mediaBinaryParameters.getMediaType()),
            MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE);
      }

      textSegments.add(TextSegment.from(mediaBinaryParameters.getLabel()));
      JSONArray jsonTextSegments = IntStream.range(0, textSegments.size())
          .mapToObj(i -> {
            JSONObject jsonSegment = new JSONObject();
            jsonSegment.put(Constants.JSON_KEY_TEXT, textSegments.get(i).text());
            JSONObject jsonMetadata = new JSONObject();
            jsonMetadata.put(Constants.JSON_KEY_INDEX, i);
            jsonSegment.put(Constants.JSON_KEY_METADATA, jsonMetadata);
            return jsonSegment;
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_TEXT_SEGMENTS, jsonTextSegments);

      jsonObject.put(Constants.JSON_KEY_EMBEDDINGS, jsonEmbeddings);
      jsonObject.put(Constants.JSON_KEY_DIMENSION,multimodalEmbeddingModelDimension);

      int finalMultimodalEmbeddingModelDimension = multimodalEmbeddingModelDimension;
      HashMap<String, Object> attributes = new HashMap<String, Object>() {{
        put("embeddingModelName", embeddingModelParameters.getEmbeddingModelName());
        put("embeddingModelDimension", finalMultimodalEmbeddingModelDimension);
        put("mediaType", mediaBinaryParameters.getMediaType());
      }};
      if(tokenUsage != null) {

        attributes.put("tokenUsage", tokenUsage);
      }
      return createMultimodalEmbeddingResponse(jsonObject.toString(), attributes);

    } catch(ModuleException me) {

      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          "Error while generating embedding from media binary",
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }
}
