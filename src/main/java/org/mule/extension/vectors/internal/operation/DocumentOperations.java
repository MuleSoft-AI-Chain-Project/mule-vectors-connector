package org.mule.extension.vectors.internal.operation;

import dev.langchain4j.data.document.DefaultDocument;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import scala.collection.Map;

import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.DocumentResponseAttributes;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.DocumentErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParameters;
import org.mule.extension.vectors.internal.helper.parameter.DocumentPayloadParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.pagination.DocumentPagingProvider;
import org.mule.extension.vectors.internal.metadata.DocumentsOutputTypeMetadataResolver;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createDocumentResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

public class DocumentOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentOperations.class);

  /**
   * Loads a single document from the storage specified by the {@code contextPath} and returns its content
   * in JSON format. The document is processed into segments based on the provided segmentation parameters.
   *
   * @param storageConfiguration the configuration for accessing the document.
   * @param storageConnection      the connection to the document storage.
   * @param documentParameters     parameters for specifying the document location and type.
   * @param segmentationParameters parameters for segmenting the document content into smaller parts.
   * @return a {@link Result} containing the document's content as an {@link InputStream} and
   *         additional metadata in {@link DocumentResponseAttributes}.
   * @throws ModuleException if an error occurs while loading or processing the document.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Document-load-single")
  @DisplayName("[Document] Load single")
  @Throws(DocumentErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/DocumentLoadSingleResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, DocumentResponseAttributes>
  loadSingleDocument(@Config StorageConfiguration storageConfiguration,
                     @Connection BaseStorageConnection storageConnection,
                     @ParameterGroup(name = "Document") DocumentParameters documentParameters,
                     @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters) {

    try {
      BaseStorage baseStorage = BaseStorage.builder()
          .configuration(storageConfiguration)
          .connection(storageConnection)
          .contextPath(documentParameters.getContextPath())
          .fileType(documentParameters.getFileType())
          .build();
      Document document = baseStorage.getSingleDocument();

      JSONObject jsonObject = JsonUtils.docToTextSegmentsJson(document,
                                                              segmentationParameters.getMaxSegmentSizeInChars(),
                                                              segmentationParameters.getMaxOverlapSizeInChars());

      return createDocumentResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("fileType", documentParameters.getFileType());
            put("contextPath", documentParameters.getContextPath());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while loading and/or segmenting document at '%s'.", documentParameters.getContextPath()),
          MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Loads a list of documents from storage based on the specified parameters, enabling
   * paginated access to the documents. The documents are segmented into smaller parts
   * according to the provided segmentation parameters.
   *
   * @param storageConfiguration the configuration for accessing the documents.
   * @param documentParameters     parameters for specifying the documents' location and type.
   * @param segmentationParameters parameters for segmenting the documents into smaller parts.
   * @param streamingHelper        helper for managing the streaming of paginated results.
   * @return a {@link PagingProvider} for streaming the paginated documents, each as a {@link Result}
   *         containing a {@link CursorProvider} for content and metadata in {@link DocumentResponseAttributes}.
   * @throws ModuleException if an error occurs while loading or segmenting the documents.
   */
  @MediaType(value = ANY, strict = false)
  @Alias("Document-load-list")
  @DisplayName("[Document] Load list")
  @Throws(DocumentErrorTypeProvider.class)
  @OutputResolver(output = DocumentsOutputTypeMetadataResolver.class)
  public PagingProvider<BaseStorageConnection, Result<CursorProvider, DocumentResponseAttributes>>
  loadDocumentList(@Config StorageConfiguration storageConfiguration,
                   @ParameterGroup(name = "Document") DocumentParameters documentParameters,
                   @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters,
                   StreamingHelper streamingHelper) {

    try {
      return new DocumentPagingProvider(storageConfiguration,
                                        documentParameters,
                                        segmentationParameters,
                                        streamingHelper);

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while loading and/or segmenting documents for path '%s'.", documentParameters.getContextPath()),
          MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Load a single document from a raw binary or base64-encoded content, without using a connection. 
   * Useful for inline or in-memory content processing. The document is processed into segments 
   * based on the provided segmentation parameters.
   *
   * @param payloadParameters parameters for specifying document passed as payload .
   * @param segmentationParameters parameters for segmenting the document content into smaller parts.
   * @return a {@link Result} containing the document's content as an {@link InputStream} and
   *         additional metadata in {@link DocumentResponseAttributes}.
   * @throws ModuleException if an error occurs while loading or processing the document.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Document-load-from-payload")
  @DisplayName("[Document] Load from payload")
  @Throws(DocumentErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/DocumentLoadSingleResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, DocumentResponseAttributes>
  loadFromPayload(@ParameterGroup(name = "Payload") DocumentPayloadParameters payloadParameters,
                  @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters) {

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

      DocumentParser documentParser = BaseStorage.getDocumentParser(payloadParameters.getFileType());
      Document document = documentParser.parse(documentStream);

      document.metadata().putAll(new HashMap<>() {{
        put("fileType", payloadParameters.getFileType());
        put("fileName", payloadParameters.getFileName());
      }});

      JSONObject jsonObject = JsonUtils.docToTextSegmentsJson(document,
                                                              segmentationParameters.getMaxSegmentSizeInChars(),
                                                              segmentationParameters.getMaxOverlapSizeInChars());

      return createDocumentResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("payloadContentFormat", payloadParameters.getFormat());
            put("fileType", payloadParameters.getFileType());
            put("contextPath", payloadParameters.getFileName());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while loading and/or segmenting document from payload."),
          MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE,
          e);
    }
  }
}
