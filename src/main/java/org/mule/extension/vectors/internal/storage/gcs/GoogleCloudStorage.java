package org.mule.extension.vectors.internal.storage.gcs;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class GoogleCloudStorage extends BaseStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorage.class);

    private final String projectId;
    private final String clientEmail;
    private final String clientId;
    private final String privateKeyId;
    private final String privateKey;
    private final String bucket;
    private final String objectKey;

    private Iterator<Blob> blobIterator;
    private Page<Blob> blobPage;

    private Storage storageService;

    public GoogleCloudStorage(StorageConfiguration storageConfiguration, GoogleCloudStorageConnection googleCloudStorageConnection,
                              String contextPath) {

        super(storageConfiguration, googleCloudStorageConnection, contextPath);
        this.projectId = googleCloudStorageConnection.getProjectId();
        this.clientEmail = googleCloudStorageConnection.getClientEmail();
        this.clientId = googleCloudStorageConnection.getClientId();
        this.privateKeyId = googleCloudStorageConnection.getPrivateKeyId();
        this.privateKey = googleCloudStorageConnection.getPrivateKey();
        String[] bucketAndObjectKey = parseContextPath(contextPath);
        this.bucket = bucketAndObjectKey[0];
        this.objectKey = bucketAndObjectKey[1];
        this.storageService = googleCloudStorageConnection.getStorageService();
    }

    private String[] parseContextPath(String contextPath) {
        if (!contextPath.toLowerCase().startsWith(Constants.GCS_PREFIX)) {
            throw new IllegalArgumentException(String.format("Invalid GCS path: '%s'. Path must start with '%s' and contain both bucket and object key.", contextPath, Constants.GCS_PREFIX));
        }
        String pathWithoutPrefix = contextPath.substring(Constants.GCS_PREFIX.length());
        int firstSlashIndex = pathWithoutPrefix.indexOf('/');
        String bucket;
        String objectKey = "";
        if (firstSlashIndex == -1) {

            bucket = pathWithoutPrefix;

        } else {

            bucket = pathWithoutPrefix.substring(0, firstSlashIndex);
            objectKey = pathWithoutPrefix.substring(firstSlashIndex + 1);
        }

        LOGGER.debug("Parsed GCS Path: Bucket = {}, Object Key = {}", bucket, objectKey);
        return new String[]{bucket, objectKey};
    }

    public File getSingleFile() {

        LOGGER.debug("GCS URL: " + contextPath);
        if (Objects.equals(this.objectKey, "")) {

            throw new ModuleException(
                String.format("GCS path must contain a bucket and object path: '%s'", contextPath),
                MuleVectorsErrorType.INVALID_PARAMETER);
        }

        InputStream inputStream = ((GoogleCloudStorageConnection) storageConnection).loadFile(this.bucket, this.objectKey);
        return new File(
            inputStream,
            this.bucket + "/" + this.objectKey,
            this.objectKey);
    }

    private void fetchNextBlobPage() {

        if(this.blobPage == null) {

            // Checks if items must be filtered by prefix or not
            if(Objects.equals(objectKey, "")){

                this.blobPage = this.storageService.list(bucket);
            } else {

                String prefix = objectKey + ((objectKey.endsWith("/") ? "" : "/"));
                this.blobPage = this.storageService.list(bucket, Storage.BlobListOption.prefix(prefix));
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
    public FileIterator fileIterator() {
        return new FileIterator();
    }

    public class FileIterator extends BaseStorage.FileIterator {

        @Override
        public File next() {
            Blob blob = getBlobIterator().next();
            LOGGER.debug("Processing GCS object key: " + blob.getName());
            InputStream content;
            try {
                content = ((GoogleCloudStorageConnection) storageConnection).loadFile(bucket, blob.getName());
            } catch (Exception e) {
                throw new ModuleException(
                    String.format("Error while loading file %s.", contextPath),
                    MuleVectorsErrorType.STORAGE_OPERATIONS_FAILURE,
                    e);
            }
            return new File(
                content,
                bucket + "/" + blob.getName(),
                blob.getName());
        }

        @Override
        public boolean hasNext() {
            return getBlobIterator().hasNext();
        }
    }
}
