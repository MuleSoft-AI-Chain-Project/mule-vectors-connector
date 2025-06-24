package org.mule.extension.vectors.internal.operation;

import org.mule.extension.vectors.api.metadata.TransformResponseAttributes;
import org.mule.extension.vectors.api.metadata.StorageResponseAttributes;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.StorageErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.FileParameters;
import org.mule.extension.vectors.internal.pagination.FilePagingProvider;
import org.mule.extension.vectors.internal.service.StorageServiceFactory;
import org.mule.extension.vectors.internal.service.storage.StorageService;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
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

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createFileResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.*;

public class StorageOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageOperations.class);

  /**
   * Loads a single file from the storage specified by the {@code contextPath} and returns its content
   * as binary.
   *
   * @param storageConfiguration the configuration for accessing the document.
   * @param storageConnection      the connection to the document storage.
   * @param fileParameters     parameters for specifying the document location and type.
   * @return a {@link Result} containing the document's content as an {@link InputStream} and
   *         additional metadata in {@link TransformResponseAttributes}.
   * @throws ModuleException if an error occurs while loading or processing the document.
   */
  @MediaType(value = APPLICATION_OCTET_STREAM, strict = false)
  @Alias("Storage-load-file")
  @DisplayName("[Storage] Load file")
  @Throws(StorageErrorTypeProvider.class)
  public Result<InputStream, StorageResponseAttributes>
  loadFile(@Config StorageConfiguration storageConfiguration,
           @Connection BaseStorageConnection storageConnection,
           @ParameterGroup(name = "File") FileParameters fileParameters) {
    try {
      StorageService storageService = StorageServiceFactory.getService(storageConfiguration, storageConnection);
      File file = storageService.getFile(fileParameters.getContextPath());
      return createFileResponse(
          file.getContent(),
          new HashMap<String, Object>() {{
            put("path", file.getPath());
            put("fileName", file.getFileName());
          }});
    } catch (ModuleException me) {
      throw me;
    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while loading and/or segmenting document at '%s'.", fileParameters.getContextPath()),
          MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Loads a list of files from storage based on the specified parameters, enabling
   * paginated access to the files.
   *
   * @param storageConfiguration the configuration for accessing the documents.
   * @param fileParameters     parameters for specifying the documents' location and type.
   * @param streamingHelper        helper for managing the streaming of paginated results.
   * @return a {@link PagingProvider} for streaming the paginated documents, each as a {@link Result}
   *         containing a {@link CursorProvider} for content and metadata in {@link TransformResponseAttributes}.
   * @throws ModuleException if an error occurs while loading or segmenting the documents.
   */
  @MediaType(value = ANY, strict = false)
  @Alias("Storage-load-file-list")
  @DisplayName("[Storage] Load file list")
  @Throws(StorageErrorTypeProvider.class)
  public PagingProvider<BaseStorageConnection, Result<CursorProvider, StorageResponseAttributes>>
  loadFileList(@Config StorageConfiguration storageConfiguration,
               @ParameterGroup(name = "Container") FileParameters fileParameters,
               StreamingHelper streamingHelper) {
    try {
      return new FilePagingProvider(storageConfiguration, fileParameters.getContextPath(), streamingHelper);
    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while loading and/or segmenting documents for path '%s'.", fileParameters.getContextPath()),
          MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE,
          e);
    }
  }
}
