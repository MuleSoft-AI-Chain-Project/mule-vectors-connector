package org.mule.extension.vectors.internal.pagination;

import org.mule.extension.vectors.api.metadata.StorageResponseAttributes;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.FileParameters;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import java.io.InputStream;
import java.util.*;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createPageFileResponse;

public class FilePagingProvider implements PagingProvider<BaseStorageConnection, Result<CursorProvider, StorageResponseAttributes>> {

  private StreamingHelper streamingHelper;
  private BaseStorage baseStorage;
  private Iterator<File> fileIterator;
  private StorageConfiguration storageConfiguration;
  private FileParameters fileParameters;

  public FilePagingProvider(StorageConfiguration storageConfiguration, FileParameters fileParameters,
                            StreamingHelper streamingHelper) {

    this.storageConfiguration = storageConfiguration;
    this.fileParameters = fileParameters;
    this.streamingHelper = streamingHelper;
  }

  @Override
  public List<Result<CursorProvider, StorageResponseAttributes>> getPage(BaseStorageConnection connection) {

    try {
      if(baseStorage == null) {

        baseStorage = BaseStorage.builder()
            .configuration(storageConfiguration)
            .connection(connection)
            .contextPath(fileParameters.getContextPath())
            .build();

        fileIterator = baseStorage.fileIterator();
      }

      while(fileIterator.hasNext()) {

        File file = fileIterator.next();

        if(file == null) continue; // Skip null document


        return createPageFileResponse(
            file.getContent(),
            new HashMap<String, Object>() {{
              put("path", file.getPath());
              put("fileName", file.getFileName());
            }},
            streamingHelper
        );

      }

      return Collections.emptyList();

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while getting document from %s.", fileParameters.getContextPath()),
          MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
          e);
    }
  }

  @Override
  public Optional<Integer> getTotalResults(BaseStorageConnection connection) {
    return java.util.Optional.empty();
  }

  @Override
  public void close(BaseStorageConnection connection) throws MuleException {

  }

  @Override
  public boolean useStickyConnections() {
    return true;
  }
}
