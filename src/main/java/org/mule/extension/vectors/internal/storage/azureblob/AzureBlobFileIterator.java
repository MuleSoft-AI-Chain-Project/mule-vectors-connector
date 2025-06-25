package org.mule.extension.vectors.internal.storage.azureblob;

import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
    public File next() {
        if (!hasNext()) throw new NoSuchElementException();
        BlobItem blobItem = getBlobIterator().next();
        InputStream content = azureClient.loadFile(container, blobItem.getName());
        return new File(content, container + "/" + blobItem.getName(), blobItem.getName());
    }
} 