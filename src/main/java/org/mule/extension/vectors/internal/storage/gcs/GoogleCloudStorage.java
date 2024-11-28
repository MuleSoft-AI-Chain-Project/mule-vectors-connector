package org.mule.extension.vectors.internal.storage.gcs;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.gcs.GoogleCloudStorageDocumentLoader;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.MetadatatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import static org.mule.extension.vectors.internal.constant.Constants.STORAGE_TYPE_GCS_PREFIX;

public class GoogleCloudStorage extends BaseStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorage.class);

    private final String gcsKeyFilePath;
    private final String gcsBucket;
    private final String gcsObjectKey;

    private GoogleCloudStorageDocumentLoader loader;
    private Storage storageServiceClient;
    private Iterator<Blob> blobIterator;
    private Page<Blob> blobPage;

    public GoogleCloudStorage(GoogleCloudStorageConfiguration googleCloudStorageConfiguration, String contextPath, String fileType) {
        super(googleCloudStorageConfiguration, contextPath, fileType);
        this.gcsKeyFilePath = googleCloudStorageConfiguration.getGcsKeyFilePath();
        String[] parsedDetails = parseContextPath(contextPath);
        this.gcsBucket = parsedDetails[0];
        this.gcsObjectKey = parsedDetails[1];
    }

    private String[] parseContextPath(String contextPath) {
        if (!contextPath.toLowerCase().startsWith(STORAGE_TYPE_GCS_PREFIX)) {
            throw new IllegalArgumentException("Invalid GCS path: " + contextPath);
        }
        String pathWithoutPrefix = contextPath.substring(STORAGE_TYPE_GCS_PREFIX.length());
        int firstSlashIndex = pathWithoutPrefix.indexOf('/');
        if (firstSlashIndex == -1) {
            throw new IllegalArgumentException("GCS path must contain a bucket and object path: " + contextPath);
        }
        String bucket = pathWithoutPrefix.substring(0, firstSlashIndex);
        String objectKey = pathWithoutPrefix.substring(firstSlashIndex + 1);
        LOGGER.debug("Parsed GCS Path: Bucket = {}, Object Key = {}", bucket, objectKey);
        return new String[]{bucket, objectKey};
    }

    private GoogleCloudStorageDocumentLoader getLoader() {
        if (loader == null) {
            try (FileInputStream keyFileStream = new FileInputStream(gcsKeyFilePath)) {
                loader = GoogleCloudStorageDocumentLoader.builder()
                        .credentials(GoogleCredentials.fromStream(keyFileStream))
                        .build();
            } catch (Exception e) {
                throw new IllegalStateException("Error initializing GCS Document Loader ", e);
            }
        }
        return loader;
    }

    private Storage getStorageServiceClient() {
        if (storageServiceClient == null) {
            try (FileInputStream keyFileStream = new FileInputStream(gcsKeyFilePath)) {
                storageServiceClient = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(keyFileStream))
                        .build()
                        .getService();
            } catch (Exception e) {
                throw new IllegalStateException("Error initializing GCS Storage Service Client ", e);
            }
        }
        return storageServiceClient;
    }

    private Iterator<Blob> getBlobIterator() {
        if (blobIterator == null || (!blobIterator.hasNext() && blobPage != null)) {
            if (blobPage == null) {
                blobPage = getStorageServiceClient().list(gcsBucket, Storage.BlobListOption.prefix(gcsObjectKey + "/"));
            } else {
                blobPage = blobPage.getNextPage();
            }
            if (blobPage == null) {
                blobIterator = Collections.emptyIterator();
            } else {
                blobIterator = StreamSupport.stream(blobPage.getValues().spliterator(), false)
                        .filter(blob -> !(blob.getName().endsWith("/") && blob.getSize() == 0))
                        .iterator();
            }
        }
        return blobIterator;
    }

    @Override
    public boolean hasNext() {
        return getBlobIterator().hasNext();
    }

    @Override
    public Document next() {
        Blob blob = getBlobIterator().next();
        LOGGER.debug(" Processing GCS object: " + blob.getName());
        Document document = getLoader().loadDocument(gcsBucket, blob.getName(), documentParser);
        MetadatatUtils.addMetadataToDocument(document, fileType, blob.getName());
        return document;
    }

    public Document getSingleDocument() {
        LOGGER.debug("GCS Key: " + gcsObjectKey);
        Document document = getLoader().loadDocument(gcsBucket, gcsObjectKey, documentParser);
        MetadatatUtils.addMetadataToDocument(document, fileType, gcsObjectKey);
        return document;
    }

}
