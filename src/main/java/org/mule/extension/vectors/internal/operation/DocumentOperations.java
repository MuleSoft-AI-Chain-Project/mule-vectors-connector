package org.mule.extension.vectors.internal.operation;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.mule.extension.vectors.internal.config.DocumentConfiguration;
import org.mule.extension.vectors.internal.util.MetadataUtils;

import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.DocumentResponseAttributes;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.DocumentErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.DocumentPayloadParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.InputJsonType;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createDocumentResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

public class DocumentOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentOperations.class);

  /**
   * Parse document from a raw binary or base64-encoded content.
   *
   * @param payloadParameters parameters for specifying document passed as payload .
   * @return a {@link Result} containing the document's content as an {@link InputStream} and
   *         additional metadata in {@link DocumentResponseAttributes}.
   * @throws ModuleException if an error occurs while loading or processing the document.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Document-parse")
  @DisplayName("[Document] Parse")
  @Throws(DocumentErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/DocumentResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, DocumentResponseAttributes>
  parse(@Config DocumentConfiguration documentConfiguration,
        @ParameterGroup(name = "Payload") DocumentPayloadParameters payloadParameters) {

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

      JSONObject jsonObject = JsonUtils.docToTextSegmentsJson(document);

      return createDocumentResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("payloadContentFormat", payloadParameters.getFormat());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while parsing document."),
          MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Chunk a text segment from a parsed document into multiple text segments based on the provided  parameters.
   * <p>
   * This operation expects an input JSON containing an array of one text segment, with associated metadata.
   * It chunks each text segment into smaller text segments based on the parameters provided, and returns
   * the result as a JSON document with the chunked text segments and metadata.
   * </p>
   *
   * @param content InputStream containing the JSON document with text segments and metadata.
   * @param segmentationParameters Parameters that define how the document should be segmented.
   * @return Result containing the chunked text segments and response attributes.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Document-chunk")
  @DisplayName("[Document] Chunk")
  @Throws(DocumentErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/DocumentResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, DocumentResponseAttributes>
  chunkDocument(@Config DocumentConfiguration documentConfiguration,
        @Alias("textSegment") @DisplayName("Text Segment") @InputJsonType(schema = "api/metadata/DocumentResponse.json") @Content InputStream content,
        @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters) {

    try {

      String contentString = IOUtils.toString(content, StandardCharsets.UTF_8);

      List<TextSegment> textSegments = new LinkedList<>();

      JSONObject jsonObject = new JSONObject(contentString);
      JSONArray jsonTextSegments = jsonObject.getJSONArray(Constants.JSON_KEY_TEXT_SEGMENTS);

      IntStream.range(0, jsonTextSegments.length())
          .mapToObj(jsonTextSegments::getJSONObject) // Convert index to JSONObject
          .forEach(jsonTextSegment -> {
            Metadata metadata = Metadata.from(jsonTextSegment.getJSONObject(Constants.JSON_KEY_METADATA).toMap());
            textSegments.add(new TextSegment(jsonTextSegment.getString(Constants.JSON_KEY_TEXT), metadata));
          });

      Document document = Document.from(textSegments.get(0).text(), textSegments.get(0).metadata());

      JSONObject responseJsonObject = JsonUtils.docToTextSegmentsJson(document,
                                                              segmentationParameters.getMaxSegmentSizeInChars(),
                                                              segmentationParameters.getMaxOverlapSizeInChars());

      return createDocumentResponse(
          responseJsonObject.toString(),
          new HashMap<String, Object>() {});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while chunking."),
          MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Chunk a text into multiple text segments based on the provided  parameters.
   * <p>
   * This operation expects an input text string.
   * It chunks each the text into smaller text segments based on the parameters provided, and returns
   * the result as an JSON document with the chunked text segments and metadata.
   * </p>
   *
   * @param text the input text to chunk.
   * @param segmentationParameters Parameters that define how the document should be segmented.
   * @return Result containing the chunked text segments and response attributes.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Text-chunk")
  @DisplayName("[Text] Chunk")
  @Throws(DocumentErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/TextListResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, DocumentResponseAttributes>
  chunkText(@Config DocumentConfiguration documentConfiguration,
        @Alias("text") @DisplayName("Text") @Content String text,
        @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters) {

    try {

      List<TextSegment> textSegments = Utils.splitTextIntoTextSegments(text,
                                                     segmentationParameters.getMaxSegmentSizeInChars(),
                                                     segmentationParameters.getMaxOverlapSizeInChars());

      JSONObject responseJsonObject = null;

      return createDocumentResponse(
          responseJsonObject.toString(),
          new HashMap<String, Object>() {});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while chunking text."),
          MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE,
          e);
    }
  }
}
