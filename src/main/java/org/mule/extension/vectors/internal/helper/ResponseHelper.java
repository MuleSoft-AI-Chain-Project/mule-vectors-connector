package org.mule.extension.vectors.internal.helper;

import org.mule.extension.vectors.api.metadata.*;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.IOUtils.toInputStream;

public final class ResponseHelper {

  private ResponseHelper() {
  }

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

  public static List<Result<CursorProvider, StoreResponseAttributes>> createPageStoreResponse(
      String response,
      Map<String, Object> storeAttributes,
      StreamingHelper streamingHelper) {

    List<Result<CursorProvider, StoreResponseAttributes>> page = new LinkedList<>();

    page.add(Result.<CursorProvider, StoreResponseAttributes>builder()
                 .attributes(new StoreResponseAttributes((HashMap<String, Object>) storeAttributes))
                 .output((CursorProvider) streamingHelper.resolveCursorProvider(toInputStream(response, StandardCharsets.UTF_8)))
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

  public static Result<InputStream, MultimodalEmbeddingResponseAttributes> createMultimodalEmbeddingResponse(
      String response,
      Map<String, Object> embeddingAttributes) {

    return Result.<InputStream, MultimodalEmbeddingResponseAttributes>builder()
        .attributes(new MultimodalEmbeddingResponseAttributes((HashMap<String, Object>) embeddingAttributes))
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

  public static Result<InputStream, DocumentResponseAttributes> createDocumentResponse(
      String response,
      Map<String, Object> documentAttributes) {

    return Result.<InputStream, DocumentResponseAttributes>builder()
        .attributes(new DocumentResponseAttributes((HashMap<String, Object>) documentAttributes))
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(toInputStream(response, StandardCharsets.UTF_8))
        .mediaType(MediaType.APPLICATION_JSON)
        .build();
  }

  public static Result<InputStream, MediaResponseAttributes> createMediaResponse(
      String response,
      Map<String, Object> mediaAttributes) {

    return Result.<InputStream, MediaResponseAttributes>builder()
        .attributes(new MediaResponseAttributes((HashMap<String, Object>) mediaAttributes))
        .attributesMediaType(MediaType.APPLICATION_JAVA)
        .output(toInputStream(response, StandardCharsets.UTF_8))
        .mediaType(MediaType.APPLICATION_JSON)
        .build();
  }

  public static List<Result<CursorProvider, StorageResponseAttributes>> createPageFileResponse(
      InputStream content,
      Map<String, Object> storageAttributes,
      StreamingHelper streamingHelper) {

    List<Result<CursorProvider, StorageResponseAttributes>> page =  new LinkedList<>();

    page.add(Result.<CursorProvider, StorageResponseAttributes>builder()
        .attributes(new StorageResponseAttributes((HashMap<String, Object>) storageAttributes))
        .output((CursorProvider) streamingHelper.resolveCursorProvider(content))
        .mediaType(MediaType.BINARY)
        .attributesMediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA)
        .build());

    return page;
  }

  public static List<Result<CursorProvider, MediaResponseAttributes>> createPageMediaResponse(
      String response,
      Map<String, Object> mediaAttributes,
      StreamingHelper streamingHelper) {

    List<Result<CursorProvider, MediaResponseAttributes>> page =  new LinkedList<>();

    page.add(Result.<CursorProvider, MediaResponseAttributes>builder()
                 .attributes(new MediaResponseAttributes((HashMap<String, Object>) mediaAttributes))
                 .output((CursorProvider) streamingHelper.resolveCursorProvider(toInputStream(response, StandardCharsets.UTF_8)))
                 .mediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON)
                 .attributesMediaType(org.mule.runtime.api.metadata.MediaType.APPLICATION_JAVA)
                 .build());

    return page;
  }
}
