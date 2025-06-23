package org.mule.extension.vectors.internal.storage;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
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
  protected FileIterator fileIterator;

  public BaseStorage(StorageConfiguration storageConfiguration, BaseStorageConnection storageConnection, String contextPath) {

    this.storageConfiguration = storageConfiguration;
    this.storageConnection = storageConnection;
    this.contextPath = contextPath;
  }

  public File getSingleFile() {
    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public static BaseStorage.Builder builder() {

    return new BaseStorage.Builder();
  }

  public FileIterator fileIterator() {
    return new FileIterator();
  }

  public static class Builder {

    private StorageConfiguration storageConfiguration;
    private BaseStorageConnection storageConnection;
    private String contextPath;

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

    public BaseStorage build() {

      BaseStorage baseStorage;

      String storageType = storageConnection == null ? Constants.STORAGE_TYPE_LOCAL : storageConnection.getStorageType();

      try {

        LOGGER.debug("Storage Type: " + storageConnection.getStorageType());
        switch (storageType) {

          case Constants.STORAGE_TYPE_LOCAL:

            baseStorage = new LocalStorage(storageConfiguration, (LocalStorageConnection) storageConnection, contextPath);
            break;

          case Constants.STORAGE_TYPE_AWS_S3:

            baseStorage = new AmazonS3Storage(storageConfiguration, (AmazonS3StorageConnection) storageConnection, contextPath);
            break;

          case Constants.STORAGE_TYPE_AZURE_BLOB:

            baseStorage = new AzureBlobStorage(storageConfiguration, (AzureBlobStorageConnection) storageConnection, contextPath);
            break;

          case Constants.STORAGE_TYPE_GCS:

            baseStorage =
                new GoogleCloudStorage(storageConfiguration, (GoogleCloudStorageConnection) storageConnection, contextPath);
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

  public class FileIterator implements Iterator<File> {

    @Override
    public boolean hasNext() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }

    @Override
    public File next() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }
  }
}
