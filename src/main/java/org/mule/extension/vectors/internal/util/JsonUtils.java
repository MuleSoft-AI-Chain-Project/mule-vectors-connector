/**
 * (c) 2003-2024 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1
 * a copy of which has been included with this distribution in the LICENSE.md file.
 * Ensure you review the LICENSE.md file included in the distribution for details.
 */
package org.mule.extension.vectors.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.ValidationUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.media.Media;
import org.mule.extension.vectors.internal.store.BaseStoreService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Utility class for JSON operations, providing methods to convert strings to JSON nodes,
 * convert collections of JSON objects to JSON arrays, and handle document segmentation.
 */
public final class JsonUtils {

  private JsonUtils() {}

  /**
   * Converts a string to a JsonNode object.
   *
   * @param content the string content to be converted.
   * @return a JsonNode representation of the content.
   * @throws IOException if an error occurs during parsing.
   */
  public static JsonNode stringToJsonNode(String content) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(content);
  }

  /**
   * Converts a collection of JSONObject instances to a JSONArray.
   *
   * @param jsonObjectList the collection of JSONObjects to be converted.
   * @return a JSONArray containing the elements of the input collection.
   */
  public static JSONArray jsonObjectCollectionToJsonArray(Collection<JSONObject> jsonObjectList) {
    JSONArray jsonArray = new JSONArray();
    for (JSONObject jsonObject : jsonObjectList) {
      jsonArray.put(jsonObject);
    }
    return jsonArray;
  }

  /**
   * Creates a JSONObject representing the ingestion status of a data source.
   *
   * @param sourceId     the unique identifier of the data source associated with the ingestion status.
   * @param embeddingIds a list of embedding identifiers related to the ingestion process.
   * @return a JSONObject containing the ingestion status, including the source ID and status.
   */
  public static JSONObject createIngestionStatusObject(String sourceId, List<String> embeddingIds) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_STATUS, Constants.OPERATION_STATUS_UPDATED);
    jsonObject.put(Constants.JSON_KEY_SOURCE_ID, sourceId);
    jsonObject.put(Constants.JSON_KEY_EMBEDDING_IDS, embeddingIds);
    return jsonObject;
  }

  /**
   * Converts a Document to a JSONObject containing its text segments.
   *
   * @param document              the Document to be processed.
   * @return a JSONObject containing text segments and their associated metadata.
   */
  public static JSONObject docToTextSegmentsJson(Document document) {

    List<TextSegment> textSegments = Collections.singletonList(document.toTextSegment());

    // Use Streams to populate a JSONArray with segment details
    JSONArray jsonTextSegments = IntStream.range(0, textSegments.size())
        .mapToObj(i -> {
          JSONObject jsonTextSegment = new JSONObject();
          jsonTextSegment.put(Constants.JSON_KEY_TEXT, textSegments.get(i).text());
          jsonTextSegment.put(Constants.JSON_KEY_METADATA, new JSONObject(textSegments.get(i).metadata().toMap()));
          return jsonTextSegment;
        })
        .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

    // Create the resulting JSON object with text segments
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_TEXT_SEGMENTS, jsonTextSegments);

    return jsonObject;
  }

  /**
   * Converts a Document to a JSONObject containing its text segments.
   * Optionally splits the document into segments of a specified maximum size and overlap.
   *
   * @param document              the Document to be processed.
   * @param maxSegmentSizeInChars the maximum size of each segment in characters.
   * @param maxOverlapSizeInChars the maximum overlap size between segments in characters.
   * @return a JSONObject containing text segments and their associated metadata.
   */
  public static JSONObject docToTextSegmentsJson(Document document, int maxSegmentSizeInChars, int maxOverlapSizeInChars) {

    List<TextSegment> textSegments;

    if (maxSegmentSizeInChars != 0 || maxOverlapSizeInChars != 0) {

      // Ensure segment and overlap sizes are positive
      ValidationUtils.ensureGreaterThanZero(maxSegmentSizeInChars, "maxSegmentSizeInChars");
      ValidationUtils.ensureGreaterThanZero(maxOverlapSizeInChars, "maxOverlapSizeInChars");

      // Split document into segments with specified parameters
      DocumentSplitter splitter = DocumentSplitters.recursive(maxSegmentSizeInChars, maxOverlapSizeInChars);
      textSegments = splitter.split(document);
    } else {
      // Use the document as a single text segment if no size constraints are provided
      textSegments = Collections.singletonList(document.toTextSegment());
    }

    // Use Streams to populate a JSONArray with segment details
    JSONArray jsonTextSegments = IntStream.range(0, textSegments.size())
        .mapToObj(i -> {
          JSONObject jsonTextSegment = new JSONObject();
          jsonTextSegment.put(Constants.JSON_KEY_TEXT, textSegments.get(i).text());
          jsonTextSegment.put(Constants.JSON_KEY_METADATA, new JSONObject(textSegments.get(i).metadata().toMap()));
          return jsonTextSegment;
        })
        .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

    // Create the resulting JSON object with text segments
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_TEXT_SEGMENTS, jsonTextSegments);

    return jsonObject;
  }

  public static JSONObject mediaToJson(Media media) {

    JSONObject jsonObject = new JSONObject();
    if(media.hasImage()) {

      jsonObject.put(Constants.JSON_KEY_BASE64DATA, media.image().base64Data());
      jsonObject.put(Constants.JSON_KEY_METADATA, new JSONObject(media.metadata().toMap()));
    }
    return jsonObject;
  }

    public static JSONObject rowToJson(BaseStoreService.Row<?> row) {

      JSONObject jsonObject = new JSONObject();
      if(row.getEmbedding() != null) jsonObject.put(Constants.JSON_KEY_EMBEDDINGS, new JSONArray(row.getEmbedding().vector()));
      jsonObject.put(Constants.JSON_KEY_METADATA, new JSONObject(((TextSegment)row.getEmbedded()).metadata().toMap()));
      jsonObject.put(Constants.JSON_KEY_TEXT, ((TextSegment)row.getEmbedded()).text());
      jsonObject.put(Constants.JSON_KEY_EMBEDDING_ID, row.getId());
      return jsonObject;
    }
}
