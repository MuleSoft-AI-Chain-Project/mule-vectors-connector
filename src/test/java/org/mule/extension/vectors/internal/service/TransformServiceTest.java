package org.mule.extension.vectors.internal.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.api.metadata.ChunkResponseAttributes;
import org.mule.extension.vectors.api.metadata.ParserResponseAttributes;
import org.mule.extension.vectors.api.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.config.TransformConfiguration;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.service.transform.TransformService;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransformServiceTest {

  @Mock
  TransformConfiguration transformConfiguration;
  @Mock
  DocumentParserParameters documentParserParameters;
  @Mock
  DocumentParser documentParser;
  @Mock
  SegmentationParameters segmentationParameters;

  TransformService service = new TransformService();

  @Test
  void parseDocument_success() throws Exception {
    when(documentParserParameters.getDocumentParser()).thenReturn(documentParser);
    when(documentParser.parse(any(InputStream.class))).thenReturn(new ByteArrayInputStream("parsed text".getBytes()));
    when(documentParserParameters.getName()).thenReturn("Test Parser");
    InputStream input = new ByteArrayInputStream("test".getBytes());
    Result<InputStream, ParserResponseAttributes> result = service.parseDocument(input, documentParserParameters);
    assertThat(result).isNotNull();
    assertThat(result.getOutput()).isNotNull();
    assertThat(new String(result.getOutput().readAllBytes())).contains("parsed text");
  }

  @Test
  void parseDocument_moduleExceptionPropagates() throws Exception {
    when(documentParserParameters.getDocumentParser()).thenReturn(documentParser);
    when(documentParser.parse(any(InputStream.class)))
        .thenThrow(new ModuleException("fail", MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE));
    InputStream input = new ByteArrayInputStream("test".getBytes());
    assertThatThrownBy(() -> service.parseDocument(input, documentParserParameters))
        .isInstanceOf(ModuleException.class)
        .getCause()
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
  void chunkText_success() throws Exception {
    when(segmentationParameters.getMaxSegmentSizeInChars()).thenReturn(10);
    when(segmentationParameters.getMaxOverlapSizeInChars()).thenReturn(2);

    // Mock the Utils method to return a JSON array as InputStream
    String jsonResponse = "[\"chunk1\",\"chunk2\"]";
    InputStream mockResponse = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));

    try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
      utils.when(() -> Utils.splitTextIntoTextSegments(any(InputStream.class), anyInt(), anyInt())).thenReturn(mockResponse);

      InputStream input = new ByteArrayInputStream("sometext".getBytes());
      Result<InputStream, ChunkResponseAttributes> result = service.chunkText(input, segmentationParameters);
      assertThat(result).isNotNull();
      assertThat(result.getOutput()).isNotNull();

      // Reset the stream for reading
      mockResponse = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
      utils.when(() -> Utils.splitTextIntoTextSegments(any(InputStream.class), anyInt(), anyInt())).thenReturn(mockResponse);

      result = service.chunkText(input, segmentationParameters);
      String json = new String(result.getOutput().readAllBytes());
      assertThat(json).isEqualTo("[\"chunk1\",\"chunk2\"]");
    }
  }

  @Test
  void chunkText_moduleExceptionPropagates() {
    when(segmentationParameters.getMaxSegmentSizeInChars()).thenReturn(10);
    when(segmentationParameters.getMaxOverlapSizeInChars()).thenReturn(2);
    try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
      utils.when(() -> Utils.splitTextIntoTextSegments(any(InputStream.class), anyInt(), anyInt()))
          .thenThrow(new ModuleException("fail", MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE));
      InputStream input = new ByteArrayInputStream("sometext".getBytes());
      assertThatThrownBy(() -> service.chunkText(input, segmentationParameters))
          .isInstanceOf(ModuleException.class)
          .getCause()
          .hasMessageContaining("fail");
    }
  }

  @Test
  void chunkText_otherExceptionIsWrapped() {
    when(segmentationParameters.getMaxSegmentSizeInChars()).thenReturn(10);
    when(segmentationParameters.getMaxOverlapSizeInChars()).thenReturn(2);
    try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
      utils.when(() -> Utils.splitTextIntoTextSegments(any(InputStream.class), anyInt(), anyInt()))
          .thenThrow(new RuntimeException("boom"));
      InputStream input = new ByteArrayInputStream("sometext".getBytes());
      assertThatThrownBy(() -> service.chunkText(input, segmentationParameters))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Error while chunking text");
    }
  }
}
