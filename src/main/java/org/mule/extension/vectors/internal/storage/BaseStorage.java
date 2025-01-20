package org.mule.extension.vectors.internal.storage;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;

import org.mule.extension.vectors.internal.config.DocumentConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorage;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorage;
import org.mule.extension.vectors.internal.storage.local.LocalStorage;
import org.mule.extension.vectors.internal.storage.amazons3.AmazonS3Storage;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public abstract class BaseStorage implements Iterator<Document> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseStorage.class);

  protected DocumentConfiguration documentConfiguration;
  protected BaseStorageConnection storageConnection;
  protected String contextPath;
  protected String fileType;

  public BaseStorage(DocumentConfiguration documentConfiguration, BaseStorageConnection storageConnection, String contextPath, String fileType) {

    this.documentConfiguration = documentConfiguration;
    this.storageConnection = storageConnection;
    this.contextPath = contextPath;
    this.fileType = fileType;
  }

  public BaseStorage(DocumentConfiguration documentConfiguration, String contextPath, String fileType) {

    this.documentConfiguration = documentConfiguration;
    this.contextPath = contextPath;
    this.fileType = fileType;
  }

  @Override
  public boolean hasNext() {
    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  @Override
  public Document next() {
    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public Document getSingleDocument() {
    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public String getStorageType() {

    return storageConnection == null ? Constants.STORAGE_TYPE_LOCAL : storageConnection.getStorageType();
  }

  protected Document getDocument(Resource resource) {

    DocumentReader documentReader = null;
    switch (fileType){

      case Constants.FILE_TYPE_TEXT:
      case Constants.FILE_TYPE_CRAWL:
      case Constants.FILE_TYPE_URL:
        documentReader = new TextReader(resource);
        break;
      case Constants.FILE_TYPE_ANY:
        documentReader = new TikaDocumentReader(resource);
        break;
      default:
        throw new IllegalArgumentException("Unsupported File Type: " + fileType);
    }
    return documentReader.get().get(0);
  }

  public static BaseStorage.Builder builder() {

    return new BaseStorage.Builder();
  }

  public static class Builder {

    private DocumentConfiguration documentConfiguration;
    private BaseStorageConnection storageConnection;
    private String contextPath;
    private String fileType;

    public Builder() {

    }

    public BaseStorage.Builder configuration(DocumentConfiguration documentConfiguration) {
      this.documentConfiguration = documentConfiguration;
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

    public BaseStorage build() {

      BaseStorage baseStorage;

      String storageType = storageConnection == null ? Constants.STORAGE_TYPE_LOCAL : storageConnection.getStorageType();

      try {

        LOGGER.debug("Storage Type: " + storageConnection.getStorageType());
        switch (storageType) {

          case Constants.STORAGE_TYPE_LOCAL:

            baseStorage = new LocalStorage(documentConfiguration, (LocalStorageConnection) storageConnection, contextPath, fileType);
            break;

          case Constants.STORAGE_TYPE_AWS_S3:

            baseStorage = new AmazonS3Storage(documentConfiguration, (AmazonS3StorageConnection) storageConnection, contextPath, fileType);
            break;

          case Constants.STORAGE_TYPE_AZURE_BLOB:

            baseStorage = new AzureBlobStorage(documentConfiguration, (AzureBlobStorageConnection) storageConnection, contextPath, fileType);
            break;

          case Constants.STORAGE_TYPE_GCS:

            baseStorage = new GoogleCloudStorage(documentConfiguration, (GoogleCloudStorageConnection) storageConnection, contextPath, fileType);
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
}
