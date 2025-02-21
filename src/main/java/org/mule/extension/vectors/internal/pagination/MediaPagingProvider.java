package org.mule.extension.vectors.internal.pagination;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.DocumentResponseAttributes;
import org.mule.extension.vectors.api.metadata.MediaResponseAttributes;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.data.Media;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParameters;
import org.mule.extension.vectors.internal.helper.parameter.MediaParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import java.util.*;

import static org.mule.extension.vectors.internal.constant.Constants.*;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.*;

public class MediaPagingProvider implements PagingProvider<BaseStorageConnection, Result<CursorProvider, MediaResponseAttributes>> {

  private StreamingHelper streamingHelper;
  private BaseStorage baseStorage;
  private Iterator<Media> mediaIterator;
  private StorageConfiguration storageConfiguration;
  private MediaParameters mediaParameters;
  private MediaProcessor mediaProcessor;

  public MediaPagingProvider(StorageConfiguration storageConfiguration, MediaParameters mediaParameters,
                             MediaProcessor mediaProcessor, StreamingHelper streamingHelper) {

    this.storageConfiguration = storageConfiguration;
    this.mediaParameters = mediaParameters;
    this.mediaProcessor = mediaProcessor;
    this.streamingHelper = streamingHelper;
  }

  @Override
  public List<Result<CursorProvider, MediaResponseAttributes>> getPage(BaseStorageConnection connection) {

    try {
      if(baseStorage == null) {

        baseStorage = BaseStorage.builder()
            .configuration(storageConfiguration)
            .connection(connection)
            .contextPath(mediaParameters.getContextPath())
            .mediaType(mediaParameters.getMediaType())
            .mediaProcessor(mediaProcessor)
            .build();

        mediaIterator = baseStorage.mediaIterator();
      }

      while(mediaIterator.hasNext()) {

        try {

          Media media = mediaIterator.next();

          JSONObject jsonObject = JsonUtils.mediaToJson(media);

          return createPageMediaResponse(
              jsonObject.toString(),
              new HashMap<String, Object>() {{
                put("mediaType", mediaParameters.getMediaType());
                put("contextPath", mediaParameters.getContextPath());
                put("fileType", media.metadata().get(METADATA_KEY_FILE_TYPE));
                put("mimeType", media.metadata().get(METADATA_KEY_MIME_TYPE));
                put("source", media.metadata().get(METADATA_KEY_SOURCE));
              }},
              streamingHelper);

        } catch (BlankDocumentException bde) {

          // Look for next page if any on error
        }

      }

      return Collections.emptyList();

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while getting media from %s.", mediaParameters.getContextPath()),
          MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
          e);
    }
  }

  @Override
  public Optional<Integer> getTotalResults(BaseStorageConnection connection) {
    return Optional.empty();
  }

  @Override
  public void close(BaseStorageConnection connection) throws MuleException {

  }

  @Override
  public boolean useStickyConnections() {
    return true;
  }
}
