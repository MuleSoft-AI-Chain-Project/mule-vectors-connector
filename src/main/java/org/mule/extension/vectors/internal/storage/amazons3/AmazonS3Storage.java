package org.mule.extension.vectors.internal.storage.amazons3;

import dev.langchain4j.data.document.BlankDocumentException;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.InputStream;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class AmazonS3Storage extends BaseStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3Storage.class);

    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    private final String awsRegion;

    private String continuationToken = null;

    private S3Client s3Client;

    private Iterator<S3Object> s3ObjectIterator;
    private ListObjectsV2Response response;

    private Iterator<S3Object> getS3ObjectIterator() {

        if(s3ObjectIterator != null && !s3ObjectIterator.hasNext() && continuationToken != null) {
            // Get the continuation token for pagination
            continuationToken = response.nextContinuationToken();
        }

        if(s3ObjectIterator == null || (!s3ObjectIterator.hasNext() && continuationToken != null)) {

            LOGGER.debug(String.format("Fetching objects from AWS S3 bucket %s with prefix %s",
                                       getAWSS3Bucket(), getAWSS3ObjectKey()));
            // Build the request
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(getAWSS3Bucket());
            String prefix = getAWSS3ObjectKey();
            if (!prefix.isEmpty()) {
                requestBuilder.prefix(prefix); // Add prefix filter only if it is not empty
                requestBuilder.delimiter("/");
            }
            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken); // Set continuation token
            }

            ListObjectsV2Request listObjectsV2Request = requestBuilder.build();
            response = s3Client.listObjectsV2(listObjectsV2Request);

            // Get the list of S3 objects and create an iterator
            this.s3ObjectIterator = response.contents().iterator();
        }
        return s3ObjectIterator;
    }

    public AmazonS3Storage(StorageConfiguration storageConfiguration, AmazonS3StorageConnection amazonS3StorageConnection,
                           String contextPath) {

        super(storageConfiguration, amazonS3StorageConnection, contextPath);
        this.awsAccessKeyId = amazonS3StorageConnection.getAwsAccessKeyId();
        this.awsSecretAccessKey = amazonS3StorageConnection.getAwsSecretAccessKey();
        this.awsRegion = amazonS3StorageConnection.getAwsRegion();
        this.s3Client = amazonS3StorageConnection.getS3Client();
    }

    public File getSingleFile() {

        LOGGER.debug("S3 URL: " + contextPath);
        InputStream inputStream = ((AmazonS3StorageConnection)storageConnection).loadFile(getAWSS3Bucket(), getAWSS3ObjectKey());
        return new File(
            inputStream,
            getAWSS3Bucket() + "/" + getAWSS3ObjectKey(),
            getAWSS3ObjectKey());
    }

    private String getAWSS3Bucket() {

        String s3Url = this.contextPath;
        // Remove the "s3://" prefix
        if (s3Url.startsWith("s3://") || s3Url.startsWith("S3://")) {
            s3Url = s3Url.substring(5);
        }
        // Extract the bucket name
        String bucket = s3Url.contains("/") ? s3Url.substring(0, s3Url.indexOf("/")) : s3Url;
        return bucket;
    }

    private String getAWSS3ObjectKey() {

        String s3Url = this.contextPath;
        // Remove the "s3://" prefix
        if (s3Url.startsWith("s3://") || s3Url.startsWith("S3://")) {
            s3Url = s3Url.substring(5);
        }
        // Extract the bucket name and object key
        int slashIndex = s3Url.indexOf("/");
        String objectKey = slashIndex != -1 ? s3Url.substring(slashIndex + 1) : "";
        return objectKey;
    }

    public BaseStorage.FileIterator fileIterator() {
        return new FileIterator();
    }

    public class FileIterator extends BaseStorage.FileIterator {

        @Override
        public boolean hasNext() {

            return getS3ObjectIterator().hasNext();
        }

        @Override
        public File next() {

            S3Object object = getS3ObjectIterator().next();
            // Skip objects that represent folders based on size
            if (object.size() == 0) {

                LOGGER.info("Skipping virtual folder: " + object.key());
                return null;
            }

            LOGGER.debug("AWS S3 Object Key: " + object.key());
            InputStream content;
            try {
                content = ((AmazonS3StorageConnection)storageConnection).loadFile(getAWSS3Bucket(), object.key());
            } catch(BlankDocumentException bde) {

                LOGGER.warn(String.format("BlankDocumentException: Error while loading file %s.", contextPath));
                throw bde;
            } catch (Exception e) {

                throw new ModuleException(
                    String.format("Error while loading file %s.", contextPath),
                    MuleVectorsErrorType.STORAGE_OPERATIONS_FAILURE,
                    e);
            }
            return new File(
                content,
                getAWSS3Bucket() + "/" + object.key(),
                object.key());
        }
    }
}
