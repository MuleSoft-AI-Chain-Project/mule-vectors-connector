package org.mule.extension.vectors.internal.service.transform;

import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONArray;
import org.mule.extension.vectors.api.metadata.TransformResponseAttributes;
import org.mule.extension.vectors.internal.config.TransformConfiguration;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createChunkedTextResponse;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.createParsedDocumentResponse;

public class TransformService {

    public Result<InputStream, TransformResponseAttributes> parseDocument(
            TransformConfiguration transformConfiguration,
            InputStream documentStream,
            DocumentParserParameters documentParserParameters) {
        try {
            String text = documentParserParameters.getDocumentParser().parse(documentStream);
            HashMap<String, Object> attributes = new HashMap<>();
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

    public Result<InputStream, TransformResponseAttributes> chunkText(
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
} 
