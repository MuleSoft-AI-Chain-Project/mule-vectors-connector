package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobFileIterator;
import com.azure.storage.blob.models.BlobItem;
import java.io.InputStream;
import java.util.List;

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
        InputStream content = azureClient.loadFile(container, blobName);
        return new File(content, container + "/" + blobName, blobName);
    }

    @Override
    public FileIterator getFileIterator(String contextPath) {
        String container = AzureBlobStorage.parseContainer(contextPath, azureClient.azureName);
        String prefix = AzureBlobStorage.parseBlobName(contextPath, azureClient.azureName);
        return new AzureBlobFileIterator(azureClient, container, prefix);
    }
} 
