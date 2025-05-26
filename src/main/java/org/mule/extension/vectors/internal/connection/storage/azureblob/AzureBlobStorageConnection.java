package org.mule.extension.vectors.internal.connection.storage.azureblob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.common.StorageSharedKeyCredential;
import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;

import static java.lang.String.format;

public class AzureBlobStorageConnection implements BaseStorageConnection {

  private String azureName;
  private String azureKey;
  private BlobServiceClient blobServiceClient;

  public AzureBlobStorageConnection(String azureName, String azureKey) {
    this.azureName = azureName;
    this.azureKey = azureKey;
  }

  public String getAzureName() {
    return azureName;
  }

  public String getAzureKey() {
    return azureKey;
  }

  public BlobServiceClient getBlobServiceClient() {
    return blobServiceClient;
  }

  @Override
  public String getStorageType() { return Constants.STORAGE_TYPE_AZURE_BLOB; }

  @Override
  public void connect() {

    this.blobServiceClient = new BlobServiceClientBuilder()
        .endpoint(String.format("https://%s.blob.core.windows.net/", azureName))
        .credential(new StorageSharedKeyCredential(azureName, azureKey))
        .buildClient();
  }

  @Override
  public void disconnect() {

    // Add logic to invalidate connection
  }

  @Override
  public boolean isValid() {

    this.blobServiceClient.listBlobContainers();
    return true;
  }

  public Document loadDocument(String containerName, String blobName, DocumentParser parser) {

    BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);
    BlobProperties properties = blobClient.getProperties();
    BlobInputStream blobInputStream = blobClient.openInputStream();
    try {

      Document document = parser.parse(blobInputStream);
      document.metadata().put(Constants.METADATA_KEY_SOURCE, format("https://%s.blob.core.windows.net/%s/%s", azureName, containerName, blobName));
      document.metadata().put("azure_storage_blob_creation_time", String.valueOf(properties.getCreationTime()));
      document.metadata().put("azure_storage_blob_last_modified", String.valueOf(properties.getLastModified()));
      document.metadata().put("azure_storage_blob_content_length", String.valueOf(properties.getBlobSize()));
      return document;

    } catch (BlankDocumentException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load document", e);
    }
  }
}
