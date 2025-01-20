package org.mule.extension.vectors.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.mule.extension.vectors.internal.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

/**
 * Utility class for handling metadata-related operations.
 */
public class LangChain4JMetadataUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(LangChain4JMetadataUtils.class);

  /**
   * Generates ingestion metadata for a document.
   *
   * @return a HashMap containing ingestion metadata, including source ID, ingestion datetime,
   *         and ingestion timestamp.
   */
  public static HashMap<String, Object> getIngestionMetadata() {

    HashMap<String, Object> ingestionMetadata = new HashMap<>();
    ingestionMetadata.put(Constants.METADATA_KEY_SOURCE_ID, dev.langchain4j.internal.Utils.randomUUID());
    ingestionMetadata.put(Constants.METADATA_KEY_INGESTION_DATETIME, Utils.getCurrentISO8601Timestamp());
    ingestionMetadata.put(Constants.METADATA_KEY_INGESTION_TIMESTAMP, Utils.getCurrentTimeMillis());
    return ingestionMetadata;
  }

  /**
   * Retrieves a display name for the document source based on its metadata.
   *
   * @param metadata the Metadata object from which the display name is derived.
   * @return a string representing the display name, which may include directory path, file name, URL, source, or title.
   */
  public static String getSourceDisplayName(Metadata metadata) {

    // Retrieve fields from metadata
    String absoluteDirectoryPath = metadata.getString("absolute_directory_path");
    String fileName = metadata.getString("file_name");
    String url = metadata.getString("url");
    String source = metadata.getString("source");
    String title = metadata.getString("title");

    // Logic to determine the result
    if (absoluteDirectoryPath != null) {
      return absoluteDirectoryPath + "/" + Optional.ofNullable(fileName).orElse("");
    } else {
      return Optional.ofNullable(url).orElse(
          Optional.ofNullable(source).orElse(title));
    }
  }

  /**
   * Adds ingestion metadata to a given document.
   *
   * @param document the Document to which ingestion metadata will be added.
   */
  public static void addIngestionMetadataToDocument(Document document) {

    document.metadata().put(Constants.METADATA_KEY_SOURCE_ID, dev.langchain4j.internal.Utils.randomUUID());
    document.metadata().put(Constants.METADATA_KEY_INGESTION_DATETIME, Utils.getCurrentISO8601Timestamp());
    document.metadata().put(Constants.METADATA_KEY_INGESTION_TIMESTAMP, Utils.getCurrentTimeMillis());
  }

  /**
   * Adds metadata to a document based on the specified file type.
   *
   * @param document the Document to which metadata is added.
   * @param fileType the type of the file (e.g., "text" or "crawl").
   */
  public static void addMetadataToDocument(Document document, String fileType) {

    if(!fileType.isEmpty()) document.metadata().put(Constants.METADATA_KEY_FILE_TYPE, fileType);

    if (fileType.equals(Constants.FILE_TYPE_CRAWL)) {

      try {

        JsonNode jsonNode = LangChain4JJsonUtils.stringToJsonNode(document.text());
        String source_url = jsonNode.path("url").asText();
        String title = jsonNode.path("title").asText();
        if(!source_url.isEmpty()) document.metadata().put(Constants.METADATA_KEY_URL, source_url);
        if(!title.isEmpty()) document.metadata().put(Constants.METADATA_KEY_TITLE, title);
      } catch (IOException ioe) {

        LOGGER.error(ioe.getMessage() + " " + Arrays.toString(ioe.getStackTrace()));
      }
    }
  }

  /**
   * Adds metadata to a document with specified file type, file name, and additional metadata.
   *
   * @param document the Document to which metadata is added.
   * @param fileType the type of the file (e.g., "text" or "any").
   * @param fileName the name of the file.
   */
  public static void addMetadataToDocument(Document document, String fileType, String fileName) {

    addMetadataToDocument(document, fileType);
    if(!fileName.isEmpty()) document.metadata().put(Constants.METADATA_KEY_FILE_NAME, fileName);
  }
}
