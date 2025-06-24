package org.mule.extension.vectors.internal.service.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobFileIterator;
import com.azure.storage.blob.models.BlobItem;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;

public class AzureBlobStorageService implements StorageService {
    private final AzureBlobStorage azureClient;

    public AzureBlobStorageService(AzureBlobStorage azureClient) {
        this.azureClient = azureClient;
    }

    @Override
    public File getFile(String path) {
        // Assume azureName is available from the client
        String container = AzureBlobStorage.parseContainer(path, azureClient.azureName);
        String blobName = AzureBlobStorage.parseBlobName(path, azureClient.azureName);

        BlobClient blobClient = azureClient.loadFile(container, blobName);
        BlobProperties properties = blobClient.getProperties();
        InputStream content = blobClient.openInputStream();
        HashMap<String, Object> metadata = new HashMap(){{
            put(Constants.METADATA_KEY_SOURCE, format("https://%s.blob.core.windows.net/%s/%s", azureClient.azureName, container, blobName));
            put("azure_storage_blob_creation_time", String.valueOf(properties.getCreationTime()));
            put("azure_storage_blob_last_modified", String.valueOf(properties.getLastModified()));
            put("azure_storage_blob_content_length", String.valueOf(properties.getBlobSize()));
        }};
        return new File(content, container + "/" + blobName, blobName, properties.getContentType(), metadata);
    }

    @Override
    public FileIterator getFileIterator(String directory) {
        String container = AzureBlobStorage.parseContainer(directory, azureClient.azureName);
        String prefix = AzureBlobStorage.parseBlobName(directory, azureClient.azureName);
        List<BlobItem> objects = azureClient.listFiles(container, prefix);
        return new AzureBlobFileIterator(azureClient, container, objects);
    }
}
