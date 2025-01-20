/**
 * (c) 2003-2024 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1
 * a copy of which has been included with this distribution in the LICENSE.md file.
 * Ensure you review the LICENSE.md file included in the distribution for details.
 */
package org.mule.extension.vectors.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.springframework.ai.document.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

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
   * Creates a JSONObject representing the ingestion status of a folder or set of files.
   *
   * @param storeName the name of the store associated with the ingestion status.
   * @return a JSONObject containing the ingestion status, including the store name and status.
   */
  public static JSONObject createIngestionStatusObject(String storeName) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_STATUS, Constants.OPERATION_STATUS_UPDATED);
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

    List<Document> documentChunks;

    if (maxSegmentSizeInChars != 0 || maxOverlapSizeInChars != 0) {

      // Ensure segment and overlap sizes are positive
      if(maxSegmentSizeInChars <= 0) throw new ModuleException("maxSegmentSizeInChars must be greater than zero", MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE);
      if(maxOverlapSizeInChars <= 0) throw new ModuleException("maxOverlapSizeInChars must be greater than zero", MuleVectorsErrorType.DOCUMENT_OPERATIONS_FAILURE);

      TokenTextSplitter splitter = new TokenTextSplitter(maxSegmentSizeInChars, 10, 10, 5000, true);
      documentChunks = splitter.split(document);

    } else {
      // Use the document as a single text segment if no size constraints are provided
      documentChunks = Collections.singletonList(document);
    }

    // Use Streams to populate a JSONArray with segment details
    JSONArray jsonTextSegments = IntStream.range(0, documentChunks.size())
        .mapToObj(i -> {
          JSONObject jsonTextSegment = new JSONObject();
          jsonTextSegment.put(Constants.JSON_KEY_TEXT, documentChunks.get(i).getText());
          jsonTextSegment.put(Constants.JSON_KEY_METADATA, new JSONObject(documentChunks.get(i).getMetadata()));
          return jsonTextSegment;
        })
        .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

    // Create the resulting JSON object with text segments
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_TEXT_SEGMENTS, jsonTextSegments);

    return jsonObject;
  }
}
