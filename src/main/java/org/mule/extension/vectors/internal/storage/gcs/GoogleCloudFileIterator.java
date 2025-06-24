package org.mule.extension.vectors.internal.storage.gcs;

import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import com.google.cloud.storage.Blob;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class GoogleCloudFileIterator implements FileIterator {
    private final GoogleCloudStorage gcsClient;
    private final String bucket;
    private final Iterator<Blob> blobIterator;

    public GoogleCloudFileIterator(GoogleCloudStorage gcsClient, String bucket, List<Blob> objects) {
        this.gcsClient = gcsClient;
        this.bucket = bucket;
        this.blobIterator = objects.iterator();
    }

    @Override
    public boolean hasNext() {
        return blobIterator.hasNext();
    }

    @Override
    public File next() {
        while (blobIterator.hasNext()) {
            Blob blob = blobIterator.next();
            if (blob.getSize() == 0 && blob.getName().endsWith("/")) {
                // Skip folders
                continue;
            }
            InputStream content = gcsClient.loadFile(bucket, blob.getName());
            return new File(content, bucket + "/" + blob.getName(), blob.getName());
        }
        return null;
    }
} 