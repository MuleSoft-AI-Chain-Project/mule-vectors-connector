package org.mule.extension.vectors.internal.operation;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONArray;
import org.mule.extension.vectors.internal.config.TransformConfiguration;

import org.mule.extension.vectors.api.metadata.DocumentResponseAttributes;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.TransformErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.DocumentPayloadParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createDocumentResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

/**
 * Provides transformation operations for document parsing and text chunking within the Mule Vectors Connector.
 * <p>
 * This class contains operations to parse documents from various payload formats and to segment text into chunks
 * according to configurable parameters. These operations are typically used in data processing and integration flows
 * where document ingestion and text segmentation are required.
 * </p>
 */
public class TransformOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransformOperations.class);

  /**
   * Parse document from a raw binary or base64-encoded content.
   *
   * @param transformConfiguration the connector configuration to use for this operation.
   * @param payloadParameters parameters for specifying document passed as payload.
   * @return a {@link Result} containing the document's content as an {@link InputStream} and
   *         additional metadata in {@link DocumentResponseAttributes}.
   * @throws ModuleException if an error occurs while loading or processing the document.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("transform-parse-document")
  @DisplayName("[Transform] Parse document")
  @Throws(TransformErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/TransformParseDocumentResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, DocumentResponseAttributes>
  parseDocument(@Config TransformConfiguration transformConfiguration,
        @ParameterGroup(name = "Payload") DocumentPayloadParameters payloadParameters) {

    // TODO: Remove LangChain4J dependency and directly use apache tika for parsing

    try {

      InputStream documentStream;

      LOGGER.debug(String.format("Payload Parameters: %s", payloadParameters));

      if (Constants.PAYLOAD_CONTENT_TYPE_BASE64.equalsIgnoreCase(payloadParameters.getFormat())) {

        byte[] decoded = Base64.getDecoder().decode(payloadParameters.getContent().toString());
        documentStream = new ByteArrayInputStream(decoded);

      } else if (Constants.PAYLOAD_CONTENT_TYPE_BINARY.equalsIgnoreCase(payloadParameters.getFormat())) {

        documentStream = payloadParameters.getContent(); // Use as-is

      } else {

        throw new IllegalArgumentException("Unsupported format: " + payloadParameters.getFormat());
      }

      DocumentParser documentParser = BaseStorage.getDocumentParser(payloadParameters.getFileParserType());
      Document document = documentParser.parse(documentStream);

      return createDocumentResponse(
          document.text(),
          new HashMap<String, Object>() {{
            put("payloadContentFormat", payloadParameters.getFormat());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while parsing document."),
          MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Chunks the provided text into multiple segments based on the segmentation parameters.
   * <p>
   * This operation splits the input text into smaller segments according to the maximum segment size and overlap size
   * specified in the segmentation parameters. The result is returned as a JSON document containing the chunked text
   * segments and associated metadata.
   * </p>
   *
   * @param transformConfiguration the connector configuration to use for this operation.
   * @param text the input text to be chunked.
   * @param segmentationParameters parameters that define how the text should be segmented, including maximum segment size and overlap size.
   * @return a {@link Result} containing the chunked text segments as an {@link InputStream} and response attributes in {@link DocumentResponseAttributes}.
   * @throws ModuleException if an error occurs during text chunking.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("transform-chunk-text")
  @DisplayName("[Transform] Chunk text")
  @Throws(TransformErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/TransformChunkTextResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, DocumentResponseAttributes>
  chunkText(@Config TransformConfiguration transformConfiguration,
        @Alias("text") @DisplayName("Text") @Content String text,
        @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters) {

    // TODO: Remove LangChain4J dependency and build internal library for chunking

    try {

      List<TextSegment> textSegments = Utils.splitTextIntoTextSegments(text,
                                                     segmentationParameters.getMaxSegmentSizeInChars(),
                                                     segmentationParameters.getMaxOverlapSizeInChars());

      JSONArray responseJsonArray = new JSONArray();

      for(TextSegment textSegment : textSegments){
        responseJsonArray.put(textSegment.text());
      }

      return createDocumentResponse(
          responseJsonArray.toString(),
          new HashMap<String, Object>() {});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while chunking text."),
          MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE,
          e);
    }
  }
}
