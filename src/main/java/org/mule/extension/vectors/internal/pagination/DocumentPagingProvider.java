package org.mule.extension.vectors.internal.pagination;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.DocumentResponseAttributes;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParameters;
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

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createPageDocumentResponse;

public class DocumentPagingProvider implements PagingProvider<BaseStorageConnection, Result<CursorProvider, DocumentResponseAttributes>> {

  private StreamingHelper streamingHelper;
  private BaseStorage baseStorage;
  private Iterator<Document> documentIterator;
  private StorageConfiguration storageConfiguration;
  private DocumentParameters documentParameters;
  private SegmentationParameters segmentationParameters;

  public DocumentPagingProvider(StorageConfiguration storageConfiguration, DocumentParameters documentParameters,
                                SegmentationParameters segmentationParameters, StreamingHelper streamingHelper) {

    this.storageConfiguration = storageConfiguration;
    this.documentParameters = documentParameters;
    this.segmentationParameters = segmentationParameters;
    this.streamingHelper = streamingHelper;
  }

  @Override
  public List<Result<CursorProvider, DocumentResponseAttributes>> getPage(BaseStorageConnection connection) {

    try {
      if(baseStorage == null) {

        baseStorage = BaseStorage.builder()
            .configuration(storageConfiguration)
            .connection(connection)
            .contextPath(documentParameters.getContextPath())
            .fileType(documentParameters.getFileType())
            .build();

        documentIterator = baseStorage.documentIterator();
      }

      while(documentIterator.hasNext()) {

        try {

          Document document = documentIterator.next();

          if(document == null) continue; // Skip null document

          JSONObject jsonObject =
              JsonUtils.docToTextSegmentsJson(document,
                                              segmentationParameters.getMaxSegmentSizeInChars(),
                                              segmentationParameters.getMaxOverlapSizeInChars());

          return createPageDocumentResponse(
              jsonObject.toString(),
              new HashMap<String, Object>() {{
                put("fileType", documentParameters.getFileType());
                put("contextPath", documentParameters.getContextPath());
              }},
              streamingHelper
          );

        } catch (BlankDocumentException bde) {

          // Look for next page if any on error
        }

      }

      return Collections.emptyList();

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while getting document from %s.", documentParameters.getContextPath()),
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
