package org.mule.extension.vectors.internal.util;


import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.media.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

/**
 * Utility class for handling metadata-related operations.
 */
public class MetadataUtils {

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
   * Adds metadata to a document based on the specified file type.
   *
   * @param document the Document to which metadata is added.
   * @param fileType the type of the file (e.g., "text" or "crawl").
   */
  public static void addMetadataToDocument(Document document, String fileType) {

    if(!fileType.isEmpty()) document.metadata().put(Constants.METADATA_KEY_FILE_TYPE, fileType);
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

  /**
   * Adds metadata to a Media object based on its type and image properties.
   *
   * @param media     The Media object to which metadata is added.
   * @param mediaType The type of the media, such as "image", "video", etc. If this value is not empty, it will be added to the
   *                  metadata.
   *
   *                  The method performs the following: - If media type is provided, it is added to the metadata under the key
   *                  {@code METADATA_KEY_MEDIA_TYPE}. - If the media has an image and the mime type is not null or empty, it adds
   *                  the mime type to the metadata under the key {@code METADATA_KEY_MIME_TYPE} and extracts the file type from
   *                  the mime type. - If the media has an image and a non-empty URL, it adds the image URL to the metadata under
   *                  the key {@code METADATA_KEY_SOURCE}. If the URL uses the "file" scheme, it extracts metadata including the
   *                  absolute directory path, filename, and file type from the file name.
   */
  public static void addImageMetadataToMedia(Media media, String mediaType) {

    if (!mediaType.isEmpty())
      media.metadata().put(Constants.METADATA_KEY_MEDIA_TYPE, mediaType);

    if (media.hasImage() && media.image().mimeType() != null && !media.image().mimeType().toString().isEmpty()) {

      String mimeType = media.image().mimeType().toString();
      media.metadata().put(Constants.METADATA_KEY_MIME_TYPE, mimeType);
      // Extract file type from mime type
      String fileTypeFromMime = mimeType.contains("/") ? mimeType.substring(mimeType.indexOf("/") + 1) : null;
      if (fileTypeFromMime != null) {
        media.metadata().put(Constants.METADATA_KEY_FILE_TYPE, fileTypeFromMime);
      }
    }

    if (media.hasImage() && !media.image().url().toString().isEmpty()) {

      URI uri = media.image().url();
      media.metadata().put(Constants.METADATA_KEY_SOURCE, media.image().url().toString());
      switch (uri.getScheme().toLowerCase()) {

        case "file":
          Path path = Paths.get(uri);
          media.metadata().put(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH, path.getParent().toString());
          media.metadata().put(Constants.METADATA_KEY_FILE_NAME, path.getFileName().toString());

          // Extract file type from file name
          String fileName = path.getFileName().toString();
          String fileTypeFromName = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : null;
          if (fileTypeFromName != null) {
            media.metadata().put(Constants.METADATA_KEY_FILE_TYPE, fileTypeFromName);
          }

      }
    }
  }
}
