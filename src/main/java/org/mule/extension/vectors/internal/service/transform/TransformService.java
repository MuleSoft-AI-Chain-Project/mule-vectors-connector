package org.mule.extension.vectors.internal.service.transform;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createChunkedTextResponse;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createParsedDocumentResponse;

import org.mule.extension.vectors.api.metadata.ChunkResponseAttributes;
import org.mule.extension.vectors.api.metadata.ParserResponseAttributes;
import org.mule.extension.vectors.api.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.util.HashMap;

public class TransformService {

  public Result<InputStream, ParserResponseAttributes> parseDocument(
                                                                     InputStream content,
                                                                     DocumentParserParameters documentParserParameters) {
    try {
      content = documentParserParameters.getDocumentParser().parse(content);
      HashMap<String, Object> attributes = new HashMap<>();
      attributes.put("documentParserName", documentParserParameters.getName());
      return createParsedDocumentResponse(
                                          content, attributes);
    } catch (Exception e) {
      throw new ModuleException("Error while parsing document.", MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE, e);
    }
  }

  public Result<InputStream, ChunkResponseAttributes> chunkText(
                                                                InputStream content,
                                                                SegmentationParameters segmentationParameters) {
    try {
      content = Utils.splitTextIntoTextSegments(content, segmentationParameters.getMaxSegmentSizeInChars(),
                                                segmentationParameters.getMaxOverlapSizeInChars());

      return createChunkedTextResponse(content);
    } catch (Exception e) {
      throw new ModuleException("Error while chunking text.", MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE, e);
    }
  }
}
