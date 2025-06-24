package org.mule.extension.vectors.internal.storage.azureblob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import com.azure.storage.blob.models.BlobItem;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

public class AzureBlobFileIterator implements FileIterator {
    private final AzureBlobStorage azureClient;
    private final String container;
    private final Iterator<BlobItem> blobIterator;

    public AzureBlobFileIterator(AzureBlobStorage azureClient, String container, List<BlobItem> objects) {
        this.azureClient = azureClient;
        this.container = container;
        this.blobIterator = objects.iterator();
    }

    @Override
    public boolean hasNext() {
        return blobIterator.hasNext();
    }

    @Override
    public File next() {
        while (blobIterator.hasNext()) {
            BlobItem blobItem = blobIterator.next();
            // Optionally skip folders or zero-size blobs if needed
            BlobClient blobClient = azureClient.loadFile(container, blobItem.getName());
            BlobProperties properties = blobClient.getProperties();
            InputStream content = blobClient.openInputStream();
            HashMap<String, Object> metadata = new HashMap(){{
                put(Constants.METADATA_KEY_SOURCE, format("https://%s.blob.core.windows.net/%s/%s", azureClient.azureName, container, blobItem.getName()));
                put("azure_storage_blob_creation_time", String.valueOf(properties.getCreationTime()));
                put("azure_storage_blob_last_modified", String.valueOf(properties.getLastModified()));
                put("azure_storage_blob_content_length", String.valueOf(properties.getBlobSize()));
            }};
            return new File(content, container + "/" + blobItem.getName(), blobItem.getName(), properties.getContentType(), metadata);
        }
        return null;
    }
} 
