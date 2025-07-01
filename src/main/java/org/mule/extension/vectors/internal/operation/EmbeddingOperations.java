package org.mule.extension.vectors.internal.operation;

import static org.mule.extension.vectors.internal.constant.Constants.MEDIA_TYPE_IMAGE;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createEmbeddingResponse;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createMultimodalEmbeddingResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.output.Response;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.EmbeddingResponseAttributes;
import org.mule.extension.vectors.api.metadata.MultimodalEmbeddingResponseAttributes;
import org.mule.extension.vectors.api.metadata.TokenUsage;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.EmbeddingErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.*;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceFactoryBuilder;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.*;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for embedding operations, providing methods to generate embeddings from text or documents.
 */
public class EmbeddingOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddingOperations.class);

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
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
  generateEmbeddingFromText(@Config EmbeddingConfiguration embeddingConfiguration,
                            @Connection BaseModelConnection modelConnection,
                            @Alias("inputs") @DisplayName("Input Texts") @Content List<String> inputs,
                            @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {

      List<TextSegment> textSegments = new LinkedList<>();
      List<Embedding> embeddings = new LinkedList<>();
      TokenUsage tokenUsage = null;
      int dimension = 0;

      for (int i = 0; i < inputs.size(); i++) {
        textSegments.add(TextSegment.from(
            inputs.get(i),
            new Metadata().put(Constants.METADATA_KEY_INDEX, i)));
      }
      Response<List<Embedding>> embeddingsResponse = new EmbeddingServiceFactoryBuilder(modelConnection).getServiceProvider().getBuilder(modelConnection, embeddingModelParameters).build().embedTexts(textSegments);

      embeddings = embeddingsResponse.content();
      tokenUsage = embeddingsResponse.tokenUsage() != null ?
          new TokenUsage(embeddingsResponse.tokenUsage().inputTokenCount() != null ? embeddingsResponse.tokenUsage().inputTokenCount() : 0,
                          embeddingsResponse.tokenUsage().outputTokenCount() != null ? embeddingsResponse.tokenUsage().outputTokenCount() : 0,
                          embeddingsResponse.tokenUsage().totalTokenCount() != null ? embeddingsResponse.tokenUsage().totalTokenCount(): 0)
          : null;

      JSONObject jsonObject = new JSONObject();

      List<TextSegment> finalTextSegments = textSegments;
      JSONArray jsonTextSegments = IntStream.range(0, textSegments.size())
          .mapToObj(i -> {
            JSONObject jsonSegment = new JSONObject();
            jsonSegment.put(Constants.JSON_KEY_TEXT, finalTextSegments.get(i).text());
            JSONObject jsonMetadata = new JSONObject();
            jsonMetadata.put(Constants.JSON_KEY_INDEX, i);
            jsonSegment.put(Constants.JSON_KEY_METADATA, jsonMetadata);
            return jsonSegment;
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_TEXT_SEGMENTS, jsonTextSegments);

      List<Embedding> finalEmbeddings = embeddings;
      JSONArray jsonEmbeddings = IntStream.range(0, embeddings.size())
          .mapToObj(i -> {
            return finalEmbeddings.get(i).vector();
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_EMBEDDINGS, jsonEmbeddings);

      dimension = embeddings.get(0).vector().length;
      jsonObject.put(Constants.JSON_KEY_DIMENSION, dimension);


      int finalDimension = dimension;

      HashMap<String, Object> attributes = new HashMap<String, Object>() {{
        put("embeddingModelName", embeddingModelParameters.getEmbeddingModelName());
        put("embeddingModelDimension", finalDimension);
      }};
      if(tokenUsage != null) {

        attributes.put("tokenUsage", tokenUsage);
      }
      return createEmbeddingResponse(jsonObject.toString(), attributes);

    } catch (ModuleException me) {
      throw me;

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

      //jsonObject.put(Constants.JSON_KEY_DIMENSION, multimodalEmbeddingModel.dimension());

      HashMap<String, Object> attributes = new HashMap<String, Object>() {{
        put("embeddingModelName", embeddingModelParameters.getEmbeddingModelName());
        //put("embeddingModelDimension", multimodalEmbeddingModel.dimension());
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
