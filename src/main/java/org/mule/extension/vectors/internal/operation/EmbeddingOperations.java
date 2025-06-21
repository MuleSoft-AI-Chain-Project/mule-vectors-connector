package org.mule.extension.vectors.internal.operation;

import static org.mule.extension.vectors.internal.constant.Constants.JSON_KEY_BASE64DATA;
import static org.mule.extension.vectors.internal.constant.Constants.MEDIA_TYPE_IMAGE;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createEmbeddingResponse;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createMultimodalEmbeddingResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import org.mule.extension.vectors.internal.helper.media.ImageProcessor;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.helper.parameter.*;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.InputJsonType;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.*;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
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

      BaseModel baseModel = BaseModel.builder()
          .configuration(embeddingConfiguration)
          .connection(modelConnection)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      List<TextSegment> textSegments = new LinkedList<>();
      List<Embedding> embeddings = new LinkedList<>();
      TokenUsage tokenUsage = null;
      int dimension = 0;

      try {

        switch(embeddingModelParameters.getEmbeddingModelType()) {

          case MULTIMODAL:

            EmbeddingMultimodalModel embeddingMultimodalModel = baseModel.buildEmbeddingMultimodalModel();
            LOGGER.debug(String.format("Embedding multimodal model for %s service built.", modelConnection.getEmbeddingModelService()));
            textSegments.add(TextSegment.from(inputs.get(0)));
            Response<Embedding> multimodalResponse = embeddingMultimodalModel.embedText(inputs.get(0));
            embeddings.add(multimodalResponse.content());
            tokenUsage = multimodalResponse.tokenUsage() != null ?
                new TokenUsage(multimodalResponse.tokenUsage().inputTokenCount(),
                               multimodalResponse.tokenUsage().outputTokenCount(),
                               multimodalResponse.tokenUsage().totalTokenCount())
                : null;
            dimension = embeddingMultimodalModel.dimension();
            break;

          case TEXT:
          default:

            EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();
            LOGGER.debug(String.format("Embedding text model for %s service built.", modelConnection.getEmbeddingModelService()));

            for (String singleText : inputs) {
              textSegments.add(TextSegment.from(singleText));
            }
            Response<List<Embedding>> textResponse = embeddingModel.embedAll(textSegments);
            embeddings = textResponse.content();
            tokenUsage = textResponse.tokenUsage() != null ?
                new TokenUsage(textResponse.tokenUsage().inputTokenCount() != null ? textResponse.tokenUsage().inputTokenCount() : 0,
                               textResponse.tokenUsage().outputTokenCount() != null ? textResponse.tokenUsage().outputTokenCount() : 0,
                               textResponse.tokenUsage().totalTokenCount() != null ? textResponse.tokenUsage().totalTokenCount(): 0)
                : null;
            dimension = embeddingModel.dimension();
            break;
        }

      }  catch(ModuleException e) {

        throw e;

      } catch(Exception e) {

        throw new ModuleException(
            String.format("Error while generating embedding from text \"%s\"", inputs),
            MuleVectorsErrorType.AI_SERVICES_FAILURE,
            e);
      }

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

  /**
   * Generates embeddings from text segments provided in a document format.
   *
   * @param embeddingConfiguration the configuration for the embedding service.
   * @param modelConnection the connection to the embedding model.
   * @param content the input text segments as an {@link InputStream} in JSON format.
   * @param embeddingModelParameters parameters for the embedding model to be used.
   * @return a {@link org.mule.runtime.extension.api.runtime.operation.Result} containing the embeddings in JSON format and metadata.
   * @throws ModuleException if an error occurs during the embedding process.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-generate-from-document")
  @DisplayName("[Embedding] Generate from document")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/EmbeddingGenerateResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
  generateEmbeddingFromDocument(@Config EmbeddingConfiguration embeddingConfiguration,
                                @Connection BaseModelConnection modelConnection,
                                @Alias("textSegments") @DisplayName("Text Segments") @InputJsonType(schema = "api/metadata/DocumentResponse.json") @Content InputStream content,
                                @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {

      String contentString = IOUtils.toString(content, StandardCharsets.UTF_8);

      BaseModel baseModel = BaseModel.builder()
          .configuration(embeddingConfiguration)
          .connection(modelConnection)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      List<TextSegment> textSegments = new LinkedList<>();
      List<Embedding> embeddings;
      TokenUsage tokenUsage;

      JSONObject jsonObject = new JSONObject(contentString);
      JSONArray jsonTextSegments = jsonObject.getJSONArray(Constants.JSON_KEY_TEXT_SEGMENTS);

      IntStream.range(0, jsonTextSegments.length())
          .mapToObj(jsonTextSegments::getJSONObject) // Convert index to JSONObject
          .forEach(jsonTextSegment -> {
            Metadata metadata = Metadata.from(jsonTextSegment.getJSONObject(Constants.JSON_KEY_METADATA).toMap());
            textSegments.add(new TextSegment(jsonTextSegment.getString(Constants.JSON_KEY_TEXT), metadata));
          });

      try {

        Response<List<Embedding>> response = embeddingModel.embedAll(textSegments);
        embeddings = response.content();
        tokenUsage = response.tokenUsage() != null ?
            new TokenUsage(response.tokenUsage().inputTokenCount() != null ? response.tokenUsage().inputTokenCount() : 0,
                           response.tokenUsage().outputTokenCount() != null ? response.tokenUsage().outputTokenCount() : 0,
                           response.tokenUsage().totalTokenCount() != null ? response.tokenUsage().totalTokenCount(): 0)
            : null;

      } catch(ModuleException e) {

        throw e;

      } catch(Exception e) {

        throw new ModuleException(
            String.format("Error while generating embedding from document \"%s\"", ""),
            MuleVectorsErrorType.AI_SERVICES_FAILURE,
            e);
      }

      JSONArray jsonEmbeddings = IntStream.range(0, embeddings.size())
          .mapToObj(i -> {
            return embeddings.get(i).vector();
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_EMBEDDINGS, jsonEmbeddings);

      jsonObject.put(Constants.JSON_KEY_DIMENSION, embeddingModel.dimension());

      HashMap<String, Object> attributes = new HashMap<String, Object>() {{
        put("embeddingModelName", embeddingModelParameters.getEmbeddingModelName());
        put("embeddingModelDimension", embeddingModel.dimension());
      }};
      if(tokenUsage != null) {

        attributes.put("tokenUsage", tokenUsage);
      }
      return createEmbeddingResponse(jsonObject.toString(), attributes);

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while generating embedding from document \"%s\"", ""),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }

  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-generate-from-binary")
  @DisplayName("[Embedding] Generate from binary")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/EmbeddingGenerateResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, MultimodalEmbeddingResponseAttributes>
  generateEmbeddingFromBinary(@Config EmbeddingConfiguration embeddingConfiguration,
                                    @Connection BaseModelConnection modelConnection,
                                    @ParameterGroup(name = "Media") MediaBinaryParameters mediaBinaryParameters,
                                    @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {


      BaseModel baseModel = BaseModel.builder()
          .configuration(embeddingConfiguration)
          .connection(modelConnection)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      // Assuming you have a multimodal embedding model method
      EmbeddingMultimodalModel multimodalEmbeddingModel = (EmbeddingMultimodalModel) baseModel.buildEmbeddingMultimodalModel();

      List<TextSegment> textSegments = new LinkedList<>();
      TokenUsage tokenUsage = null;

      JSONObject jsonObject = new JSONObject();

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

      JSONArray jsonEmbeddings = new JSONArray();

      // Convert InputStream to byte array
      byte[] mediaBytes = IOUtils.toByteArray(mediaBinaryParameters.getBinaryInputStream());

      MediaProcessor mediaProcessor = null;

      if(mediaBinaryParameters.getMediaProcessorParameters() != null) {

        if(mediaBinaryParameters.getMediaType().equals(MEDIA_TYPE_IMAGE)) {

          ImageProcessorParameters imageProcessorParameters =
              (ImageProcessorParameters) mediaBinaryParameters.getMediaProcessorParameters();

          mediaProcessor = ImageProcessor.builder()
              .targetWidth(imageProcessorParameters.getTargetWidth())
              .targetHeight(imageProcessorParameters.getTargetHeight())
              .compressionQuality(imageProcessorParameters.getCompressionQuality())
              .scaleStrategy(imageProcessorParameters.getScaleStrategy())
              .build();

          mediaBytes = mediaProcessor.process(mediaBytes);
        }
      }

      if(mediaBinaryParameters.getMediaType().equals(MEDIA_TYPE_IMAGE)) {

        Response<Embedding> response = mediaBinaryParameters.getLabel() != null && !mediaBinaryParameters.getLabel().isEmpty() ?
            multimodalEmbeddingModel.embedTextAndImage(mediaBinaryParameters.getLabel(), mediaBytes) :
            multimodalEmbeddingModel.embedImage(mediaBytes);
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

      jsonObject.put(Constants.JSON_KEY_EMBEDDINGS, jsonEmbeddings);

      jsonObject.put(Constants.JSON_KEY_DIMENSION, multimodalEmbeddingModel.dimension());

      HashMap<String, Object> attributes = new HashMap<String, Object>() {{
        put("embeddingModelName", embeddingModelParameters.getEmbeddingModelName());
        put("embeddingModelDimension", multimodalEmbeddingModel.dimension());
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

  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-generate-from-media")
  @DisplayName("[Embedding] Generate from media")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/EmbeddingGenerateResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, MultimodalEmbeddingResponseAttributes>
  generateEmbeddingFromMedia(@Config EmbeddingConfiguration embeddingConfiguration,
                                    @Connection BaseModelConnection modelConnection,
                                    @Alias("media") @DisplayName("Media") @InputJsonType(schema = "api/metadata/MediaLoadSingleResponse.json") @Content InputStream mediaContent,
                                    @Alias("label") @DisplayName("Media Label") @Summary("Short text describing the image") @Example("An image of a sunset") @Content String label,
                                    @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {

      String mediaContentString = IOUtils.toString(mediaContent, StandardCharsets.UTF_8);
      JSONObject jsonMediaObject = new JSONObject(mediaContentString);

      JSONObject jsonObject = new JSONObject();

      List<TextSegment> textSegments = new LinkedList<>();
      textSegments.add(TextSegment.from(label));
      JSONArray jsonTextSegments = IntStream.range(0, textSegments.size())
          .mapToObj(i -> {
            JSONObject jsonSegment = new JSONObject();
            jsonSegment.put(Constants.JSON_KEY_TEXT, textSegments.get(i).text());
            JSONObject jsonMetadata = jsonMediaObject.getJSONObject(Constants.JSON_KEY_METADATA);
            jsonMetadata.put(Constants.JSON_KEY_INDEX, i);
            jsonSegment.put(Constants.JSON_KEY_METADATA, jsonMetadata);
            return jsonSegment;
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_TEXT_SEGMENTS, jsonTextSegments);

      BaseModel baseModel = BaseModel.builder()
          .configuration(embeddingConfiguration)
          .connection(modelConnection)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      // Assuming you have a multimodal embedding model method
      EmbeddingMultimodalModel multimodalEmbeddingModel = (EmbeddingMultimodalModel) baseModel.buildEmbeddingMultimodalModel();

      Embedding embedding;
      TokenUsage tokenUsage;
      String mediaType = jsonMediaObject.getJSONObject(Constants.JSON_KEY_METADATA).getString(Constants.METADATA_KEY_MEDIA_TYPE);
      if (mediaType.equals(MEDIA_TYPE_IMAGE)) {

        Response<Embedding> response = label != null && !label.isEmpty()
            ?
            multimodalEmbeddingModel.embedTextAndImage(label,
                                                       Base64.getDecoder().decode(jsonMediaObject.getString(JSON_KEY_BASE64DATA)))
            :
                multimodalEmbeddingModel.embedImage(Base64.getDecoder().decode(jsonMediaObject.getString(JSON_KEY_BASE64DATA)));
        embedding = response.content();
        tokenUsage = response.tokenUsage() != null ?
            new TokenUsage(response.tokenUsage().inputTokenCount() != null ? response.tokenUsage().inputTokenCount() : 0,
                           response.tokenUsage().outputTokenCount() != null ? response.tokenUsage().outputTokenCount() : 0,
                           response.tokenUsage().totalTokenCount() != null ? response.tokenUsage().totalTokenCount(): 0)
            : null;
      } else {
        throw new ModuleException(
            String.format("Media type %s not supported.", mediaType),
            MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE);
      }

      JSONArray jsonEmbeddings = new JSONArray();
      jsonEmbeddings.put(embedding.vector());

      jsonObject.put(Constants.JSON_KEY_EMBEDDINGS, jsonEmbeddings);

      jsonObject.put(Constants.JSON_KEY_DIMENSION, multimodalEmbeddingModel.dimension());


      HashMap<String, Object> attributes = new HashMap<String, Object>() {{
        put("embeddingModelName", embeddingModelParameters.getEmbeddingModelName());
        put("embeddingModelDimension", multimodalEmbeddingModel.dimension());
      }};
      if (tokenUsage != null) {

        attributes.put("tokenUsage", tokenUsage);
      }
      return createMultimodalEmbeddingResponse(jsonObject.toString(), attributes);

    } catch(ModuleException me) {

      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          "Error while generating embedding from image and text",
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }
}
