package org.mule.extension.vectors.internal.operation;

import dev.langchain4j.data.document.Document;
import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.DocumentResponseAttributes;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.DocumentErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParameters;
import org.mule.extension.vectors.internal.metadata.DocumentsOutputTypeMetadataResolver;
import org.mule.extension.vectors.internal.pagination.DocumentPagingProvider;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createDocumentResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

public class StorageOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageOperations.class);

  /**
   * Loads a single document from the storage specified by the {@code contextPath} and returns its content
   * in JSON format.
   *
   * @param storageConfiguration the configuration for accessing the document.
   * @param storageConnection      the connection to the document storage.
   * @param documentParameters     parameters for specifying the document location and type.
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
                     @ParameterGroup(name = "Document") DocumentParameters documentParameters) {

    try {

      BaseStorage baseStorage = BaseStorage.builder()
          .configuration(storageConfiguration)
          .connection(storageConnection)
          .contextPath(documentParameters.getContextPath())
          .fileType(documentParameters.getFileType())
          .build();
      Document document = baseStorage.getSingleDocument();

      JSONObject jsonObject = JsonUtils.docToTextSegmentsJson(document);

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
   * paginated access to the documents.
   *
   * @param storageConfiguration the configuration for accessing the documents.
   * @param documentParameters     parameters for specifying the documents' location and type.
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
                   StreamingHelper streamingHelper) {

    try {
      return new DocumentPagingProvider(storageConfiguration,
                                        documentParameters,
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
}
