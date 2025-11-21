package org.mule.extension.vectors.internal.pagination;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createPageFileResponse;

import org.mule.extension.vectors.api.metadata.StorageResponseAttributes;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.provider.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.store.StoreOperationsHelper;
import org.mule.extension.vectors.internal.service.StorageServiceFactory;
import org.mule.extension.vectors.internal.service.storage.StorageService;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FilePagingProvider
    implements PagingProvider<BaseStorageConnection, Result<CursorProvider<Cursor>, StorageResponseAttributes>> {

  private final StorageConfiguration storageConfiguration;
  private final String contextPath;
  private final StreamingHelper streamingHelper;
  private FileIterator fileIterator;

  public FilePagingProvider(StorageConfiguration storageConfiguration, String directory, StreamingHelper streamingHelper) {
    this.storageConfiguration = storageConfiguration;
    this.contextPath = directory;
    this.streamingHelper = streamingHelper;
  }

  @Override
  public List<Result<CursorProvider<Cursor>, StorageResponseAttributes>> getPage(BaseStorageConnection connection) {
    try {
      if (fileIterator == null) {
        StorageService storageService = StorageServiceFactory.getService(
                                                                         storageConfiguration, connection);
        fileIterator = storageService.getFileIterator(contextPath);
      }
      while (fileIterator.hasNext()) {
        FileInfo file = fileIterator.next();
        if (file == null)
          continue;
        return createPageFileResponse(
                                      file.getContent(),
                                      StoreOperationsHelper.getMetadataMap(file),
                                      streamingHelper);
      }
      return Collections.emptyList();
    } catch (ModuleException me) {
      throw me;
    } catch (Exception e) {
      throw new ModuleException(
                                String.format("Error while getting document from %s.", contextPath),
                                MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
                                e);
    }
  }

  @Override
  public Optional<Integer> getTotalResults(BaseStorageConnection connection) {
    return java.util.Optional.empty();
  }

  @Override
  public void close(BaseStorageConnection connection) throws MuleException {}

  @Override
  public boolean useStickyConnections() {
    return true;
  }
}
