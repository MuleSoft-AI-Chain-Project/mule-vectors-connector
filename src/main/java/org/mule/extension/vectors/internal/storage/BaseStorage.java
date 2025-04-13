package org.mule.extension.vectors.internal.storage;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.Media;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorage;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorage;
import org.mule.extension.vectors.internal.storage.local.LocalStorage;
import org.mule.extension.vectors.internal.storage.amazons3.AmazonS3Storage;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public abstract class BaseStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseStorage.class);

  protected StorageConfiguration storageConfiguration;
  protected BaseStorageConnection storageConnection;
  protected String contextPath;
  protected String fileType;
  protected DocumentParser documentParser;
  protected String mediaType;
  protected MediaProcessor mediaProcessor;
  protected DocumentIterator documentIterator;
  protected MediaIterator mediaIterator;

  public BaseStorage(StorageConfiguration storageConfiguration, BaseStorageConnection storageConnection, String contextPath,
                     String fileType, String mediaType, MediaProcessor mediaProcessor) {

    this.storageConfiguration = storageConfiguration;
    this.storageConnection = storageConnection;
    this.contextPath = contextPath;
    this.fileType = fileType;
    this.mediaType = mediaType;
    if (fileType != null)
      this.documentParser = getDocumentParser(fileType);
    this.mediaProcessor = mediaProcessor;
  }

  public Document getSingleDocument() {
    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public Media getSingleMedia() {
    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public String getStorageType() {

    return storageConnection == null ? Constants.STORAGE_TYPE_LOCAL : storageConnection.getStorageType();
  }

  protected DocumentParser getDocumentParser(String fileType) {

    DocumentParser documentParser = null;
    switch (fileType) {

      case Constants.FILE_TYPE_TEXT:
      case Constants.FILE_TYPE_CRAWL:
      case Constants.FILE_TYPE_URL:
        documentParser = new TextDocumentParser();
        break;
      case Constants.FILE_TYPE_ANY:
        documentParser = new ApacheTikaDocumentParser();
        break;
      default:
        throw new IllegalArgumentException("Unsupported File Type: " + fileType);
    }
    return documentParser;
  }

  public static BaseStorage.Builder builder() {

    return new BaseStorage.Builder();
  }

  public DocumentIterator documentIterator() {
    return new DocumentIterator();
  }

  public MediaIterator mediaIterator() {
    return new MediaIterator();
  }

  public static class Builder {

    private StorageConfiguration storageConfiguration;
    private BaseStorageConnection storageConnection;
    private String contextPath;
    private String fileType;
    private String mediaType;
    private MediaProcessor mediaProcessor;

    public Builder() {

    }

    public BaseStorage.Builder configuration(StorageConfiguration storageConfiguration) {
      this.storageConfiguration = storageConfiguration;
      return this;
    }

    public BaseStorage.Builder connection(BaseStorageConnection storageConnection) {
      this.storageConnection = storageConnection;
      return this;
    }

    public BaseStorage.Builder contextPath(String contextPath) {
      this.contextPath = contextPath;
      return this;
    }

    public BaseStorage.Builder fileType(String fileType) {
      this.fileType = fileType;
      return this;
    }

    public BaseStorage.Builder mediaType(String mediaType) {
      this.mediaType = mediaType;
      return this;
    }

    public BaseStorage.Builder mediaProcessor(MediaProcessor mediaProcessor) {
      this.mediaProcessor = mediaProcessor;
      return this;
    }

    public BaseStorage build() {

      BaseStorage baseStorage;

      String storageType = storageConnection == null ? Constants.STORAGE_TYPE_LOCAL : storageConnection.getStorageType();

      try {

        LOGGER.debug("Storage Type: " + storageConnection.getStorageType());
        switch (storageType) {

          case Constants.STORAGE_TYPE_LOCAL:

            baseStorage = new LocalStorage(storageConfiguration, (LocalStorageConnection) storageConnection, contextPath,
                                           fileType, mediaType, mediaProcessor);
            break;

          case Constants.STORAGE_TYPE_AWS_S3:

            baseStorage = new AmazonS3Storage(storageConfiguration, (AmazonS3StorageConnection) storageConnection, contextPath,
                                              fileType, mediaType, mediaProcessor);
            break;

          case Constants.STORAGE_TYPE_AZURE_BLOB:

            baseStorage = new AzureBlobStorage(storageConfiguration, (AzureBlobStorageConnection) storageConnection, contextPath,
                                               fileType, mediaType, mediaProcessor);
            break;

          case Constants.STORAGE_TYPE_GCS:

            baseStorage =
                new GoogleCloudStorage(storageConfiguration, (GoogleCloudStorageConnection) storageConnection, contextPath,
                                       fileType, mediaType, mediaProcessor);
            break;

          default:

            throw new ModuleException(
                String.format("Error while initializing storage. Type \"%s\" is not supported.", storageType),
                MuleVectorsErrorType.STORAGE_SERVICES_FAILURE);
        }

      } catch (ModuleException e) {

        throw e;

      } catch (Exception e) {

        throw new ModuleException(
            String.format("Error while initializing storage type \"%s\".", storageType),
            MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
            e);
      }
      return baseStorage;
    }
  }

  public class DocumentIterator implements Iterator<Document> {

    @Override
    public boolean hasNext() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }

    @Override
    public Document next() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }
  }

  public class MediaIterator implements Iterator<Media> {

    @Override
    public boolean hasNext() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }

    @Override
    public Media next() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }
  }
}
