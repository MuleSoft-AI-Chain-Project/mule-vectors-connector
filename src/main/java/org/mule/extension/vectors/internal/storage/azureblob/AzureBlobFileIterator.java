package org.mule.extension.vectors.internal.storage.azureblob;

import static java.lang.String.format;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.storage.FileIterator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;

public class AzureBlobFileIterator implements FileIterator {
    private final AzureBlobStorage azureClient;
    private final String container;
    private final String prefix;
    private Iterator<BlobItem> blobIterator;

    public AzureBlobFileIterator(AzureBlobStorage azureClient, String container, String prefix) {
        this.azureClient = azureClient;
        this.container = container;
        this.prefix = prefix;
        this.blobIterator = null;
    }

    private Iterator<BlobItem> getBlobIterator() {
        if (blobIterator == null) {
            blobIterator = azureClient.getBlobServiceClient()
                .getBlobContainerClient(container)
                .listBlobs(new ListBlobsOptions().setPrefix(prefix), null)
                .iterator();
        }
        return blobIterator;
    }

    @Override
    public boolean hasNext() {
        return getBlobIterator().hasNext();
    }

    @Override
    public FileInfo next() {
        if (!hasNext()) throw new NoSuchElementException();
        BlobItem blobItem = getBlobIterator().next();
        InputStream content = azureClient.loadFile(container, blobItem.getName());
        BlobClient blobClient = azureClient.getBlonbClient();
        BlobProperties properties = blobClient.getProperties();
        HashMap<String, Object> metadata = new HashMap(){{
            put(Constants.METADATA_KEY_SOURCE, format("https://%s.blob.core.windows.net/%s/%s", azureClient.azureName, container, blobItem.getName()));
            put("azure_storage_blob_creation_time", String.valueOf(properties.getCreationTime()));
            put("azure_storage_blob_last_modified", String.valueOf(properties.getLastModified()));
            put("azure_storage_blob_content_length", String.valueOf(properties.getBlobSize()));
        }};
        return new FileInfo(content, container + "/" + blobItem.getName(), blobItem.getName(), properties.getContentType(), metadata);
    }
} 
