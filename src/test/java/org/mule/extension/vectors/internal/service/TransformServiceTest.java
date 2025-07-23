package org.mule.extension.vectors.internal.service;

import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mule.extension.vectors.internal.config.TransformConfiguration;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.service.transform.TransformService;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

@ExtendWith(MockitoExtension.class)
class TransformServiceTest {

    @Mock TransformConfiguration transformConfiguration;
    @Mock DocumentParserParameters documentParserParameters;
    @Mock DocumentParser documentParser;
    @Mock SegmentationParameters segmentationParameters;

    TransformService service = new TransformService();

    @Test
    void parseDocument_success() throws Exception {
        when(documentParserParameters.getDocumentParser()).thenReturn(documentParser);
        when(documentParser.parse(any(InputStream.class))).thenReturn("parsed text");
        InputStream input = new ByteArrayInputStream("test".getBytes());
        Result<InputStream, Map<String, Object>> result = service.parseDocument(input, documentParserParameters);
        assertThat(result).isNotNull();
        assertThat(result.getOutput()).isNotNull();
        assertThat(new String(result.getOutput().readAllBytes())).contains("parsed text");
    }

    @Test
    void parseDocument_moduleExceptionPropagates() throws Exception {
        when(documentParserParameters.getDocumentParser()).thenReturn(documentParser);
        when(documentParser.parse(any(InputStream.class))).thenThrow(new ModuleException("fail", MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE));
        InputStream input = new ByteArrayInputStream("test".getBytes());
        assertThatThrownBy(() -> service.parseDocument(input, documentParserParameters))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("fail");
    }

    @Test
    void parseDocument_otherExceptionIsWrapped() throws Exception {
        when(documentParserParameters.getDocumentParser()).thenReturn(documentParser);
        when(documentParser.parse(any(InputStream.class))).thenThrow(new RuntimeException("boom"));
        InputStream input = new ByteArrayInputStream("test".getBytes());
        assertThatThrownBy(() -> service.parseDocument(input, documentParserParameters))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Error while parsing document");
    }

    @Test
    void chunkText_success() throws java.io.IOException {
        when(segmentationParameters.getMaxSegmentSizeInChars()).thenReturn(10);
        when(segmentationParameters.getMaxOverlapSizeInChars()).thenReturn(2);
        List<TextSegment> segments = List.of(
            dev.langchain4j.data.segment.TextSegment.from("chunk1"),
            dev.langchain4j.data.segment.TextSegment.from("chunk2")
        );
        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            utils.when(() -> Utils.splitTextIntoTextSegments(anyString(), anyInt(), anyInt())).thenReturn(segments);
            Result<InputStream,  Map<String, Object>> result = service.chunkText("sometext", segmentationParameters);
            assertThat(result).isNotNull();
            String json = new String(result.getOutput().readAllBytes());
            JSONArray arr = new JSONArray(json);
            assertThat(arr.length()).isEqualTo(2);
            assertThat(arr.getString(0)).isEqualTo("chunk1");
            assertThat(arr.getString(1)).isEqualTo("chunk2");
        }
    }

    @Test
    void chunkText_moduleExceptionPropagates() {
        when(segmentationParameters.getMaxSegmentSizeInChars()).thenReturn(10);
        when(segmentationParameters.getMaxOverlapSizeInChars()).thenReturn(2);
        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            utils.when(() -> Utils.splitTextIntoTextSegments(anyString(), anyInt(), anyInt()))
                    .thenThrow(new ModuleException("fail", MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE));
            assertThatThrownBy(() -> service.chunkText("sometext", segmentationParameters))
                    .isInstanceOf(ModuleException.class)
                    .hasMessageContaining("fail");
        }
    }

    @Test
    void chunkText_otherExceptionIsWrapped() {
        when(segmentationParameters.getMaxSegmentSizeInChars()).thenReturn(10);
        when(segmentationParameters.getMaxOverlapSizeInChars()).thenReturn(2);
        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            utils.when(() -> Utils.splitTextIntoTextSegments(anyString(), anyInt(), anyInt()))
                    .thenThrow(new RuntimeException("boom"));
            assertThatThrownBy(() -> service.chunkText("sometext", segmentationParameters))
                    .isInstanceOf(ModuleException.class)
                    .hasMessageContaining("Error while chunking text");
        }
    }
} 
