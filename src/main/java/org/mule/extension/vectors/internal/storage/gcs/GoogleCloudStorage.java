package org.mule.extension.vectors.internal.storage.gcs;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import org.springframework.ai.document.Document;

import org.mule.extension.vectors.internal.config.DocumentConfiguration;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
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

    private Storage storageService;
    private Iterator<Blob> blobIterator;
    private Page<Blob> blobPage;

    public GoogleCloudStorage(DocumentConfiguration documentConfiguration, GoogleCloudStorageConnection googleCloudStorageConnection, String contextPath, String fileType) {
        super(documentConfiguration, googleCloudStorageConnection, contextPath, fileType);
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

    private String buildJsonCredentials() {
            return new StringBuilder()
                    .append("{")
                    .append("\"type\": \"service_account\",")
                    .append("\"project_id\": \"").append(this.projectId).append("\",")
                    .append("\"private_key_id\": \"").append(this.privateKeyId).append("\",")
                    .append("\"private_key\": \"").append(this.privateKey).append("\",")
                    .append("\"client_email\": \"").append(this.clientEmail).append("\",")
                    .append("\"client_id\": \"").append(this.clientId).append("\",")
                    .append("\"auth_uri\": \"").append(Constants.GCP_AUTH_URI).append("\",")
                    .append("\"token_uri\": \"").append(Constants.GCP_TOKEN_URI).append("\",")
                    .append("\"auth_provider_x509_cert_url\": \"").append(Constants.GCP_AUTH_PROVIDER_X509_CERT_URL).append("\",")
                    .append("\"client_x509_cert_url\": \"").append(Constants.GCP_CLIENT_X509_CERT_URL).append(this.clientEmail).append("\",")
                    .append("\"universe_domain\": \"googleapis.com\"")
                    .append("}")
                    .toString();
    }

    private Storage getStorageService() {
        if (this.storageService == null) {
            try {
                ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(buildJsonCredentials().getBytes()));
                this.storageService = StorageOptions.newBuilder()
                        .setCredentials(serviceAccountCredentials)
                        .build()
                        .getService();
            } catch (IOException e) {
                throw new ModuleException(
                        String.format("Error initializing GCS Storage Service."),
                        MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
                        e);
            }
        }
        return this.storageService;
    }

    private Iterator<Blob> getBlobIterator() {
        if (this.blobIterator == null || (!this.blobIterator.hasNext() && this.blobPage != null)) {
            fetchNextBlobPage();
        }
        return this.blobIterator;
    }

    private void fetchNextBlobPage() {

        if(this.blobPage == null) {

            // Checks if items must be filtered by prefix or not
            if(Objects.equals(this.objectKey, "")){

                this.blobPage = getStorageService().list(this.bucket);
            } else {

                String prefix = this.objectKey + ((this.objectKey.endsWith("/") ? "" : "/"));
                this.blobPage = getStorageService().list(this.bucket, Storage.BlobListOption.prefix(prefix));
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

    @Override
    public Document next() {
        Blob blob = getBlobIterator().next();
        LOGGER.debug("Processing GCS object key: " + blob.getName());
        Document document;
        try {
            Resource gcsResource = getResource(this.bucket, blob.getName());
            document = getDocument(gcsResource);

        } catch (Exception e) {
            throw new ModuleException(
                    String.format("Error while parsing document %s.", contextPath),
                    MuleVectorsErrorType.DOCUMENT_PARSING_FAILURE,
                    e);
        }
        MetadataUtils.addMetadataToDocument(document, fileType, blob.getName());
        return document;
    }

    @Override
    public boolean hasNext() {
        return getBlobIterator().hasNext();
    }

    public Document getSingleDocument() {
        LOGGER.debug("GCS URL: " + contextPath);
        if (Objects.equals(this.objectKey, "")) {

            throw new ModuleException(
                String.format("GCS path must contain a bucket and object path: '%s'", contextPath),
                MuleVectorsErrorType.INVALID_PARAMETERS_ERROR);
        }
        Resource gcsResource = getResource(this.bucket, this.objectKey);
        Document document = getDocument(gcsResource);
        MetadataUtils.addMetadataToDocument(document, fileType, this.objectKey);
        return document;
    }

    /**
     * Fetches a blob from GCS as an InputStreamResource for streaming large files.
     *
     * @param bucketName Name of the bucket
     * @param blobName   Name of the blob (object key)
     * @return InputStreamResource
     */
    public Resource getResource(String bucketName, String blobName) {

        // Get the blob
        Blob blob = getStorageService().get(bucketName, blobName);

        // Check if the blob exists
        if (blob == null) {
            throw new IllegalArgumentException("Blob not found: " + blobName + " in bucket: " + bucketName);
        }

        // Get the InputStream using blob.reader()
        InputStream inputStream = Channels.newInputStream(blob.reader());

        // Wrap it in InputStreamResource
        return new InputStreamResource(inputStream);
    }
}
