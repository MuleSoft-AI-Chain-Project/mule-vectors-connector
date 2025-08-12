package org.mule.extension.vectors.internal.service.transform;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createChunkedTextResponse;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createParsedDocumentResponse;
import static org.mule.extension.vectors.internal.constant.Constants.MEDIA_TYPE_IMAGE;

import org.mule.extension.vectors.api.metadata.ChunkResponseAttributes;
import org.mule.extension.vectors.api.metadata.ParserResponseAttributes;
import org.mule.extension.vectors.api.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.media.ImageProcessor;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.helper.parameter.ImageProcessorParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.helper.parameter.TransformMediaBinaryParameters;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;

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

  public Result<InputStream, ParserResponseAttributes> processMedia(
            TransformMediaBinaryParameters mediaBinaryParameters) {
        try {
            byte[] mediaBytes = IOUtils.toByteArray(mediaBinaryParameters.getBinaryInputStream());
            MediaProcessor mediaProcessor = null;
            if (mediaBinaryParameters.getMediaProcessorParameters() != null) {
                if (mediaBinaryParameters.getMediaType().equals(MEDIA_TYPE_IMAGE)) {
                    ImageProcessorParameters imageProcessorParameters =
                            (ImageProcessorParameters) mediaBinaryParameters.getMediaProcessorParameters();
                    mediaProcessor = ImageProcessor.builder()
                            .targetWidth(imageProcessorParameters.getTargetWidth())
                            .targetHeight(imageProcessorParameters.getTargetHeight())
                            .compressionQuality(imageProcessorParameters.getCompressionQuality())
                            .scaleStrategy(imageProcessorParameters.getScaleStrategy())
                            .build();
                    mediaBytes = mediaProcessor.process(mediaBytes);
                }
            }
            return createProcessedMediaResponse(
                    new ByteArrayInputStream(mediaBytes),
                    new HashMap<String, Object>() {{
                        put("mediaType", mediaBinaryParameters.getMediaType());
                    }}
            );
        } catch (ModuleException me) {
            throw me;
        } catch (Exception e) {
            throw new ModuleException(
                    "Error while processing media",
                    MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE,
                    e);
        }
    }
}
