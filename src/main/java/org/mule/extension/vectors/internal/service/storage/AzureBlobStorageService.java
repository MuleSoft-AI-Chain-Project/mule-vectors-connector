package org.mule.extension.vectors.internal.service.storage;

import static java.lang.String.format;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobFileIterator;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorage;

import java.io.InputStream;
import java.util.HashMap;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;

public class AzureBlobStorageService implements StorageService {

  private final AzureBlobStorage azureClient;

  public AzureBlobStorageService(AzureBlobStorage azureClient) {
    this.azureClient = azureClient;
  }

  @Override
  public FileInfo getFile(String path) {
    // Assume azureName is available from the client
    String container = AzureBlobStorage.parseContainer(path, azureClient.azureName);
    String blobName = AzureBlobStorage.parseBlobName(path, azureClient.azureName);
    BlobClient blobClient = azureClient.getBlonbClient();
    BlobProperties properties = blobClient.getProperties();
    HashMap<String, Object> metadata = new HashMap<>();
    metadata.put(Constants.METADATA_KEY_SOURCE,
                 format("https://%s.blob.core.windows.net/%s/%s", azureClient.azureName, container, blobName));
    metadata.put("azure_storage_blob_creation_time", String.valueOf(properties.getCreationTime()));
    metadata.put("azure_storage_blob_last_modified", String.valueOf(properties.getLastModified()));
    metadata.put("azure_storage_blob_content_length", String.valueOf(properties.getBlobSize()));

    InputStream content = azureClient.loadFile(container, blobName);
    return new FileInfo(content, container + "/" + blobName, blobName, properties.getContentType(), metadata);
  }

  @Override
  public FileIterator getFileIterator(String contextPath) {
    String container = AzureBlobStorage.parseContainer(contextPath, azureClient.azureName);
    String prefix = AzureBlobStorage.parseBlobName(contextPath, azureClient.azureName);
    return new AzureBlobFileIterator(azureClient, container, prefix);
  }
}
