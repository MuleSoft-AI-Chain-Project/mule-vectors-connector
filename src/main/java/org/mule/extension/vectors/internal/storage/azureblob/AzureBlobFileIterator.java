package org.mule.extension.vectors.internal.storage.azureblob;

import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import com.azure.storage.blob.models.BlobItem;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

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
            InputStream content = azureClient.loadFile(container, blobItem.getName());
            return new File(content, container + "/" + blobItem.getName(), blobItem.getName());
        }
        return null;
    }
} 