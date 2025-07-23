package org.mule.extension.vectors.internal.service.transform;

import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONArray;
import org.mule.extension.vectors.api.metadata.ChunkResponseAttributes;
import org.mule.extension.vectors.api.metadata.ParserResponseAttributes;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.media.ImageProcessor;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.helper.parameter.*;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createChunkedTextResponse;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createParsedDocumentResponse;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createProcessedMediaResponse;

public class TransformService {

    public Result<InputStream, ParserResponseAttributes> parseDocument(
            InputStream documentStream,
            DocumentParserParameters documentParserParameters) {
        try {
            String text = documentParserParameters.getDocumentParser().parse(documentStream);
            HashMap<String, Object> attributes = new HashMap<>();
            attributes.put("documentParserName", documentParserParameters.getName());
            return createParsedDocumentResponse(
                    text,
                    attributes);
        } catch (ModuleException me) {
            throw me;
        } catch (Exception e) {
            throw new ModuleException(
                    "Error while parsing document.",
                    MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE,
                    e);
        }
    }

    public Result<InputStream, ChunkResponseAttributes> chunkText(
            String text,
            SegmentationParameters segmentationParameters) {
        try {
            List<TextSegment> textSegments = Utils.splitTextIntoTextSegments(text,
                    segmentationParameters.getMaxSegmentSizeInChars(),
                    segmentationParameters.getMaxOverlapSizeInChars());
            JSONArray responseJsonArray = new JSONArray();
            for (TextSegment textSegment : textSegments) {
                responseJsonArray.put(textSegment.text());
            }
            HashMap<String, Object> attributes = new HashMap<>();
            return createChunkedTextResponse(
                    responseJsonArray.toString(),
                    attributes);
        } catch (ModuleException me) {
            throw me;
        } catch (Exception e) {
            throw new ModuleException(
                    "Error while chunking text.",
                    MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE,
                    e);
        }
    }

    public Result<InputStream, TransformResponseAttributes> processMedia(
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
 
