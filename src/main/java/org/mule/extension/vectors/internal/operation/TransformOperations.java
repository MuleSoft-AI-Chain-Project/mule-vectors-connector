package org.mule.extension.vectors.internal.operation;
import org.mule.extension.vectors.api.metadata.ChunkResponseAttributes;
import org.mule.extension.vectors.api.metadata.ParserResponseAttributes;
import org.mule.extension.vectors.internal.config.TransformConfiguration;
import org.mule.extension.vectors.internal.error.provider.TransformErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.SegmentationParameters;
import org.mule.extension.vectors.internal.helper.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.service.transform.TransformService;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.*;

/**
 * Provides transformation operations for document parsing and text chunking within the Mule Vectors Connector.
 * <p>
 * This class contains operations to parse documents from various payload formats and to segment text into chunks
 * according to configurable parameters. These operations are typically used in data processing and integration flows
 * where document ingestion and text segmentation are required.
 * </p>
 */
public class TransformOperations {

  private final TransformService transformService = new TransformService();

/**
 * Parse document from a raw binary or base64-encoded content.
 *
 * @param transformConfiguration the configuration for the transformation.
 * @param documentStream the input stream containing the document to parse.
 * @return a {@link Result} containing the document's content as an {@link InputStream} and
 *         additional metadata in {@link TransformResponseAttributes}.
 * @throws ModuleException if an error occurs while loading or processing the document.
 */
  @MediaType(value = TEXT_PLAIN, strict = false)
  @Alias("Transform-parse-document")
  @DisplayName("[Transform] Parse document")
  @Throws(TransformErrorTypeProvider.class)
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, ParserResponseAttributes>
  parseDocument(@Config TransformConfiguration transformConfiguration,
                @Alias("documentBinary") @DisplayName("Document binary") @Content(primary = true) InputStream documentStream,
                @Alias("documentParserParameters") @DisplayName("Document parser") DocumentParserParameters documentParserParameters) {
    return transformService.parseDocument(documentStream, documentParserParameters);
  }

  /**
   * Chunks the provided text into multiple segments based on the segmentation parameters.
   * <p>
   * This operation splits the input text into smaller segments according to the maximum segment size and overlap size
   * specified in the segmentation parameters. The result is returned as a JSON document containing the chunked text
   * segments and associated metadata.
   * </p>
   *
   * @param text the input text to be chunked.
   * @param segmentationParameters parameters that define how the text should be segmented, including maximum segment size and overlap size.
   * @return a {@link Result} containing the chunked text segments as an {@link InputStream} and response attributes in {@link TransformResponseAttributes}.
   * @throws ModuleException if an error occurs during text chunking.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Transform-chunk-text")
  @DisplayName("[Transform] Chunk text")
  @Throws(TransformErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/TransformChunkTextResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, ChunkResponseAttributes>
  chunkText(@Alias("text") @DisplayName("Text") @Content String text,
            @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters) {
    return transformService.chunkText(text, segmentationParameters);
  }
}
