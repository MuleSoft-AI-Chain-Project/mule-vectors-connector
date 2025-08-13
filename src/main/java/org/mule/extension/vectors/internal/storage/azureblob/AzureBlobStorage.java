package org.mule.extension.vectors.internal.storage.azureblob;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;

import java.io.InputStream;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobInputStream;

public class AzureBlobStorage {

  public static final String HTTPS_S_BLOB_CORE_WINDOWS_NET = "https://%s.blob.core.windows.net/";
  public final String azureName;
  private final BlobServiceClient blobServiceClient;
  BlobClient blobClient;

  public AzureBlobStorage(StorageConfiguration storageConfiguration, AzureBlobStorageConnection azureBlobStorageConnection) {
    this.azureName = azureBlobStorageConnection.getAzureName();
    this.blobServiceClient = azureBlobStorageConnection.getBlobServiceClient();
  }

  public InputStream loadFile(String containerName, String blobName) {
    blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);
    BlobProperties properties = blobClient.getProperties();
    BlobInputStream blobInputStream = blobClient.openInputStream();
    return blobInputStream;
  }

  public BlobClient getBlonbClient() {
    return blobClient;
  }

  public static String parseContainer(String azureBlobStorageUrl, String azureName) {
    String endpoint = String.format(HTTPS_S_BLOB_CORE_WINDOWS_NET, azureName);
    if (azureBlobStorageUrl.startsWith(endpoint)) {
      azureBlobStorageUrl = azureBlobStorageUrl.substring(endpoint.length());
    }
    return azureBlobStorageUrl.contains("/") ? azureBlobStorageUrl.substring(0, azureBlobStorageUrl.indexOf("/"))
        : azureBlobStorageUrl;
  }

  public static String parseBlobName(String azureBlobStorageUrl, String azureName) {
    String endpoint = String.format(HTTPS_S_BLOB_CORE_WINDOWS_NET, azureName);
    if (azureBlobStorageUrl.startsWith(endpoint)) {
      azureBlobStorageUrl = azureBlobStorageUrl.substring(endpoint.length());
    }
    int slashIndex = azureBlobStorageUrl.indexOf("/");
    return slashIndex != -1 ? azureBlobStorageUrl.substring(slashIndex + 1) : "";
  }

  public BlobServiceClient getBlobServiceClient() {
    return blobServiceClient;
  }
}
