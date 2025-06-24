package org.mule.extension.vectors.internal.storage.azureblob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AzureBlobStorage {
    public final String azureName;
    private final String azureKey;
    private final BlobServiceClient blobServiceClient;

    public AzureBlobStorage(StorageConfiguration storageConfiguration, AzureBlobStorageConnection azureBlobStorageConnection, String contextPath) {
        this.azureName = azureBlobStorageConnection.getAzureName();
        this.azureKey = azureBlobStorageConnection.getAzureKey();
        this.blobServiceClient = azureBlobStorageConnection.getBlobServiceClient();
    }

    public InputStream loadFile(String containerName, String blobName) {
        BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);
        BlobProperties properties = blobClient.getProperties();
        BlobInputStream blobInputStream = blobClient.openInputStream();
        return blobInputStream;
    }

    public List<BlobItem> listFiles(String containerName, String prefix) {
        List<BlobItem> result = new ArrayList<>();
        Iterator<BlobItem> iterator = blobServiceClient.getBlobContainerClient(containerName)
                .listBlobs(new ListBlobsOptions().setPrefix(prefix), null).iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    public static String parseContainer(String azureBlobStorageUrl, String azureName) {
        String endpoint = String.format("https://%s.blob.core.windows.net/", azureName);
        if (azureBlobStorageUrl.startsWith(endpoint)) {
            azureBlobStorageUrl = azureBlobStorageUrl.substring(endpoint.length());
        }
        return azureBlobStorageUrl.contains("/") ? azureBlobStorageUrl.substring(0, azureBlobStorageUrl.indexOf("/")) : azureBlobStorageUrl;
    }

    public static String parseBlobName(String azureBlobStorageUrl, String azureName) {
        String endpoint = String.format("https://%s.blob.core.windows.net/", azureName);
        if (azureBlobStorageUrl.startsWith(endpoint)) {
            azureBlobStorageUrl = azureBlobStorageUrl.substring(endpoint.length());
        }
        int slashIndex = azureBlobStorageUrl.indexOf("/");
        return slashIndex != -1 ? azureBlobStorageUrl.substring(slashIndex + 1) : "";
    }
}
