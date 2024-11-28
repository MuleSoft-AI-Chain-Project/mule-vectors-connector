package org.mule.extension.vectors.internal.storage;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorage;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorageConfiguration;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorage;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorageConfiguration;
import org.mule.extension.vectors.internal.storage.local.LocalStorage;
import org.mule.extension.vectors.internal.storage.local.LocalStorageConfiguration;
import org.mule.extension.vectors.internal.storage.s3.AWSS3Storage;
import org.mule.extension.vectors.internal.storage.s3.AWSS3StorageConfiguration;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public abstract class BaseStorage implements Iterator<Document> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseStorage.class);

  protected BaseStorageConfiguration storageConfiguration;
  protected String contextPath;
  protected String fileType;
  protected DocumentParser documentParser;

  public BaseStorage(BaseStorageConfiguration storageConfiguration, String contextPath, String fileType) {

    this.storageConfiguration = storageConfiguration;
    this.contextPath = contextPath;
    this.fileType = fileType;
    this.documentParser = getDocumentParser(fileType);
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

    return storageConfiguration == null ? Constants.STORAGE_TYPE_LOCAL : storageConfiguration.getStorageType();
  }

  protected DocumentParser getDocumentParser(String fileType) {

    DocumentParser documentParser = null;
    switch (fileType){

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

  public static class Builder {

    private BaseStorageConfiguration storageConfiguration;
    private String contextPath;
    private String fileType;

    public Builder() {

    }

    public BaseStorage.Builder storageConfiguration(BaseStorageConfiguration storageConfiguration) {
      this.storageConfiguration = storageConfiguration;
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

      String storageType = storageConfiguration == null ? Constants.STORAGE_TYPE_LOCAL : storageConfiguration.getStorageType();

      try {

        LOGGER.debug("Storage Type: " + storageConfiguration.getStorageType());
        switch (storageType) {

          case Constants.STORAGE_TYPE_LOCAL:

            baseStorage = new LocalStorage((LocalStorageConfiguration) storageConfiguration, contextPath, fileType);
            break;

          case Constants.STORAGE_TYPE_AWS_S3:

            baseStorage = new AWSS3Storage((AWSS3StorageConfiguration) storageConfiguration, contextPath, fileType);
            break;

          case Constants.STORAGE_TYPE_AZURE_BLOB:

            baseStorage = new AzureBlobStorage((AzureBlobStorageConfiguration) storageConfiguration, contextPath, fileType);
            break;

          case Constants.STORAGE_TYPE_GCS:

            baseStorage = new GoogleCloudStorage((GoogleCloudStorageConfiguration) storageConfiguration, contextPath, fileType);
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
