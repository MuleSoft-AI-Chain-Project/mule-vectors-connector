package org.mule.extension.vectors.internal.storage.local;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.image.Image;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.media.Media;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

public class LocalStorage extends BaseStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorage.class);

  private final String fullPath;

  private List<Path> pathList;
  private Iterator<Path> pathIterator;

  public LocalStorage(StorageConfiguration storageConfiguration, LocalStorageConnection storageConnection, String contextPath,
                      String fileType, String mediaType, MediaProcessor mediaProcessor) {

    super(storageConfiguration, storageConnection, contextPath, fileType, mediaType, mediaProcessor);
    this.fullPath = storageConnection.getWorkingDir() != null ? storageConnection.getWorkingDir() + "/" + contextPath : contextPath;
  }

  public InputStream getSingleFile() {

    Path path = Paths.get(fullPath);
    return ((LocalStorageConnection)storageConnection).loadFile(path);
  }

  /**
   * Retrieves a single media item.
   *
   * @return a Media object representing the media item.
   */
  public Media getSingleMedia() {
    Path path = Paths.get(fullPath);
    Media media;

    switch (mediaType) {

      case Constants.MEDIA_TYPE_IMAGE:
        media = Media.fromImage(loadImage(path));
        MetadataUtils.addImageMetadataToMedia(media, mediaType);
        break;

      default:
        throw new IllegalArgumentException("Unsupported Media Type: " + mediaType);
    }
    return media;
  }

  /**
   * Loads an image from the specified file path.
   *
   * @param path The file path to the image to be loaded.
   * @return An Image object containing the image data, MIME type, URL, and Base64-encoded string.
   * @throws ModuleException If there is an error reading the image file or determining its MIME type.
   */
  private Image loadImage(Path path) {

    Image image;

    try {

      URI imageUri = path.toUri();
      String mimeType = Files.probeContentType(path); // Attempt to determine the MIME type
      // Fallback if mimeType is null
      if (mimeType == null) {
        mimeType = Utils.getMimeTypeFallback(path);
      }
      byte[] imageBytes = Files.readAllBytes(path);

      String format = mimeType.contains("/") ? mimeType.substring(mimeType.indexOf("/") + 1) : null;
      if(mediaProcessor!= null) imageBytes = mediaProcessor.process(imageBytes, format);
      String base64Data = Base64.getEncoder().encodeToString(imageBytes);

      image = Image.builder()
          .url(imageUri)
          .mimeType(mimeType)
          .base64Data(base64Data)
          .build();

    } catch (IOException ioe) {

      throw new ModuleException(String.format("Impossible to load the image from %s", path.toString()),
                                MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
                                ioe);
    }

    LOGGER.debug(String.format("Image uri: %s, Image mime type: %s", image.url().toString(), image.mimeType()));

    return image;
  }

  @Override
  public FileIterator documentIterator() {
    return new FileIterator();
  }

  @Override
  public MediaIterator mediaIterator() {
    return new MediaIterator();
  }

  private Iterator<Path> getPathIterator() {
    if (pathList == null) {  // Only load files if not already loaded
      try (Stream<Path> paths = Files.walk(Paths.get(fullPath))) {
        // Collect all files as a list
        pathList = paths.filter(Files::isRegularFile).collect(Collectors.toList());
        // Create an iterator for the list of files
        pathIterator = pathList.iterator();
      } catch (IOException e) {
        throw new ModuleException(
            String.format("Error while getting document from %s.", fullPath),
            MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
            e);
      }
    }
    return pathIterator;
  }

  public class FileIterator extends BaseStorage.FileIterator {

    // Override hasNext to check if there are files left to process
    @Override
    public boolean hasNext() {
      return getPathIterator() != null && getPathIterator().hasNext();
    }

    // Override next to return the next document
    @Override
    public InputStream next() {
      if (hasNext()) {
        Path path = getPathIterator().next();
        LOGGER.debug("File: " + path.getFileName().toString());
        InputStream content;
        try {
          content = ((LocalStorageConnection)storageConnection).loadFile(path);
        } catch(BlankDocumentException bde) {

          LOGGER.warn(String.format("BlankDocumentException: Error while loading file %s.", path.toString()));
          throw bde;
        } catch (Exception e) {

          throw new ModuleException(
              String.format("Error while loading file %s.", path.toString()),
              MuleVectorsErrorType.STORAGE_OPERATIONS_FAILURE,
              e);
        }
        return content;
      }
      throw new IllegalStateException("No more files to iterate");
    }
  }

  public class MediaIterator extends BaseStorage.MediaIterator {

    // Override hasNext to check if there are files left to process
    @Override
    public boolean hasNext() {
      return getPathIterator() != null && getPathIterator().hasNext();
    }

    // Override next to return the next document
    @Override
    public Media next() {
      if (hasNext()) {
        Path path = getPathIterator().next();
        LOGGER.debug("Media file: " + path.getFileName().toString());
        Media media;
        try {

          media = Media.fromImage(loadImage(path));
          MetadataUtils.addImageMetadataToMedia(media, mediaType);

        } catch (Exception e) {

          throw new ModuleException(
              String.format("Error while loading file %s.", path.toString()),
              MuleVectorsErrorType.DOCUMENT_PARSING_FAILURE,
              e);
        }
        return media;
      }
      throw new IllegalStateException("No more media files to iterate");
    }
  }
}
