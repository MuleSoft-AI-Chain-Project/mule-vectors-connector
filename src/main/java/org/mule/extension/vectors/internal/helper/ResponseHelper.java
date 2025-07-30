package org.mule.extension.vectors.internal.helper;

import org.mule.extension.vectors.api.metadata.*;
import static org.apache.commons.io.IOUtils.toInputStream;

import org.mule.extension.vectors.api.metadata.ChunkResponseAttributes;
import org.mule.extension.vectors.api.metadata.EmbeddingResponseAttributes;
import org.mule.extension.vectors.api.metadata.ParserResponseAttributes;
import org.mule.extension.vectors.api.metadata.StoreResponseAttributes;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ResponseHelper {

  private ResponseHelper() {}

  public static Result<InputStream, StoreResponseAttributes> createStoreResponse(
                                                                                 String response,
                                                                                 Map<String, Object> storeAttributes) {

    return Result.<InputStream, StoreResponseAttributes>builder()
        .attributes(new StoreResponseAttributes((HashMap<String, Object>) storeAttributes))
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(toInputStream(response, StandardCharsets.UTF_8))
        .mediaType(MediaType.APPLICATION_JSON)
        .build();
  }

  public static List<Result<CursorProvider<Cursor>, StoreResponseAttributes>> createPageStoreResponse(
                                                                                                      String response,
                                                                                                      Map<String, Object> storeAttributes,
                                                                                                      StreamingHelper streamingHelper) {

    List<Result<CursorProvider<Cursor>, StoreResponseAttributes>> page = new LinkedList<>();

    page.add(Result.<CursorProvider<Cursor>, StoreResponseAttributes>builder()
        .attributes(new StoreResponseAttributes((HashMap<String, Object>) storeAttributes))
        .output((CursorProvider<Cursor>) streamingHelper.resolveCursorProvider(toInputStream(response, StandardCharsets.UTF_8)))
        .mediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON)
        .attributesMediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA)
        .build());

    return page;
  }

  public static Result<InputStream, EmbeddingResponseAttributes> createEmbeddingResponse(
                                                                                         String response,
                                                                                         Map<String, Object> embeddingAttributes) {

    return Result.<InputStream, EmbeddingResponseAttributes>builder()
        .attributes(new EmbeddingResponseAttributes((HashMap<String, Object>) embeddingAttributes))
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(toInputStream(response, StandardCharsets.UTF_8))
        .mediaType(MediaType.APPLICATION_JSON)
        .build();
  }


  public static Result<InputStream, StorageResponseAttributes> createFileResponse(
      InputStream content,
      Map<String, Object> storageAttributes) {

    return Result.<InputStream, StorageResponseAttributes>builder()
        .attributes(new StorageResponseAttributes((HashMap<String, Object>) storageAttributes))
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(content)
        .mediaType(MediaType.BINARY)
        .build();
  }

  public static Result<InputStream, ParserResponseAttributes> createParsedDocumentResponse(
                                                                                           InputStream response,
                                                                                           Map<String, Object> documentAttributes) {

    return Result.<InputStream, ParserResponseAttributes>builder()
        .attributes(new ParserResponseAttributes((HashMap<String, Object>) documentAttributes))
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(response)
        .mediaType(MediaType.TEXT)
        .build();
  }

  public static Result<InputStream, ChunkResponseAttributes> createChunkedTextResponse(InputStream content) {

    return Result.<InputStream, ChunkResponseAttributes>builder()
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(content)
        .mediaType(MediaType.APPLICATION_JSON)
        .build();
  }

  public static List<Result<CursorProvider<Cursor>, StorageResponseAttributes>> createPageFileResponse(
      InputStream content,
      Map<String, Object> storageAttributes,
      StreamingHelper streamingHelper) {

    List<Result<CursorProvider<Cursor>, StorageResponseAttributes>> page =  new LinkedList<>();

    page.add(Result.<CursorProvider<Cursor>, StorageResponseAttributes>builder()
        .attributes(new StorageResponseAttributes((HashMap<String, Object>) storageAttributes))
        .output((CursorProvider<Cursor>) streamingHelper.resolveCursorProvider(content))
        .mediaType(MediaType.BINARY)
        .attributesMediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA)
        .build());

    return page;
  }

  public static Result<InputStream, TransformResponseAttributes> createProcessedMediaResponse(
      InputStream content,
      Map<String, Object> transformAttributes) {

    return Result.<InputStream, TransformResponseAttributes>builder()
        .attributes(new TransformResponseAttributes((HashMap<String, Object>) transformAttributes))
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(content)
        .mediaType(MediaType.BINARY)
        .build();
  }
}
