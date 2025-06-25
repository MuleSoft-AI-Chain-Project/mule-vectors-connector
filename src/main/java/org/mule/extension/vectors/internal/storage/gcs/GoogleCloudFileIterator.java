package org.mule.extension.vectors.internal.storage.gcs;

import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class GoogleCloudFileIterator implements FileIterator {
    private final GoogleCloudStorage gcsClient;
    private final String bucket;
    private final String objectKey;

    private Page<Blob> blobPage;
    private Iterator<Blob> blobIterator;

    public GoogleCloudFileIterator(GoogleCloudStorage gcsClient, String bucket, String objectKey) {
        this.gcsClient = gcsClient;
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.blobPage = null;
        this.blobIterator = null;
    }

    private void fetchNextBlobPage() {
        if (this.blobPage == null) {
            if (Objects.equals(objectKey, "")) {
                this.blobPage = gcsClient.getStorageService().list(bucket);
            } else {
                String prefix = objectKey + ((objectKey.endsWith("/") ? "" : "/"));
                this.blobPage = gcsClient.getStorageService().list(bucket, com.google.cloud.storage.Storage.BlobListOption.prefix(prefix));
            }
        } else {
            this.blobPage = this.blobPage.getNextPage();
        }
        this.blobIterator = (this.blobPage == null)
            ? Collections.emptyIterator()
            : StreamSupport.stream(this.blobPage.getValues().spliterator(), false)
                .filter(blob -> !(blob.getName().endsWith("/") && blob.getSize() == 0))
                .iterator();
    }

    private Iterator<Blob> getBlobIterator() {
        if (blobIterator == null || (!blobIterator.hasNext() && blobPage != null)) {
            fetchNextBlobPage();
        }
        return blobIterator;
    }

    @Override
    public boolean hasNext() {
        return getBlobIterator().hasNext();
    }

    @Override
    public File next() {
        Blob blob = getBlobIterator().next();
        InputStream content = gcsClient.loadFile(bucket, blob.getName());
        return new File(content, bucket + "/" + blob.getName(), blob.getName());
    }
} 