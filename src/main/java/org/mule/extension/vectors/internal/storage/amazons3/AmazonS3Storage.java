package org.mule.extension.vectors.internal.storage.amazons3;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.image.Image;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.Media;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import dev.langchain4j.data.document.loader.amazon.s3.AmazonS3DocumentLoader;
import dev.langchain4j.data.document.loader.amazon.s3.AwsCredentials;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;

import dev.langchain4j.data.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class AmazonS3Storage extends BaseStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3Storage.class);

    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    private final String awsRegion;

    private String continuationToken = null;

    private AwsCredentials getCredentials() {
        return new AwsCredentials(awsAccessKeyId, awsSecretAccessKey);
    }

    private AmazonS3DocumentLoader loader;

    private AmazonS3DocumentLoader getLoader() {

        if(loader == null) {

            loader = new AmazonS3DocumentLoader(s3Client);
        }
        return loader;
    }

    private S3Client s3Client;

    private S3Client getS3Client() {

        if(s3Client == null) {

            // Create S3 client with your credentials
            this.s3Client = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)))
                .build();
        }
        return s3Client;
    }

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
            response = getS3Client().listObjectsV2(listObjectsV2Request);

            // Get the list of S3 objects and create an iterator
            this.s3ObjectIterator = response.contents().iterator();
        }
        return s3ObjectIterator;
    }

    public AmazonS3Storage(StorageConfiguration storageConfiguration, AmazonS3StorageConnection amazonS3StorageConnection,
                           String contextPath, String fileType, String mediaType, MediaProcessor mediaProcessor) {

        super(storageConfiguration, amazonS3StorageConnection, contextPath, fileType, mediaType, mediaProcessor);
        this.awsAccessKeyId = amazonS3StorageConnection.getAwsAccessKeyId();
        this.awsSecretAccessKey = amazonS3StorageConnection.getAwsSecretAccessKey();
        this.awsRegion = amazonS3StorageConnection.getAwsRegion();
        this.s3Client = amazonS3StorageConnection.getS3Client();
    }

    public Document getSingleDocument() {

        LOGGER.debug("S3 URL: " + contextPath);
        Document document = getLoader().loadDocument(getAWSS3Bucket(), getAWSS3ObjectKey(), documentParser);
        MetadataUtils.addMetadataToDocument(document, fileType, getAWSS3ObjectKey());
        return document;
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

    public Media getSingleMedia() {

        Media media;

        switch (mediaType) {

            case Constants.MEDIA_TYPE_IMAGE:

                media = Media.fromImage(loadImage(getAWSS3Bucket(), getAWSS3ObjectKey()));
                MetadataUtils.addImageMetadataToMedia(media, mediaType);
                break;

            default:
                throw new IllegalArgumentException("Unsupported Media Type: " + mediaType);
        }
        return media;
    }

    private Image loadImage(String bucketName, String objectKey) {

        Image image;

        try {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = getS3Client().getObjectAsBytes(getObjectRequest);
            GetObjectResponse response = objectBytes.response();

            String mimeType = response.contentType();
            byte[] imageBytes = objectBytes.asByteArray();

            String format = mimeType.contains("/") ? mimeType.substring(mimeType.indexOf("/") + 1) : null;
            if(mediaProcessor!= null) imageBytes = mediaProcessor.process(imageBytes, format);
            String base64Data = Base64.getEncoder().encodeToString(imageBytes);

            // Encode only special characters, but keep `/`
            String encodedObjectKey = URLEncoder.encode(objectKey, "UTF-8")
                .replace("+", "%20") // Fix space encoding
                .replace("%2F", "/"); // Keep `/` in the path

            image = Image.builder()
                .url("s3://" + bucketName + "/" + encodedObjectKey)
                .mimeType(mimeType)
                .base64Data(base64Data)
                .build();

        } catch (Exception ioe) {

            throw new ModuleException(String.format("Impossible to load the image from %s", ""),
                                      MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
                                      ioe);
        }
        return image;
    }

    @Override
    public DocumentIterator documentIterator() {
        return new DocumentIterator();
    }

    @Override
    public MediaIterator mediaIterator() {
        return new MediaIterator();
    }

    public class DocumentIterator extends BaseStorage.DocumentIterator {

        @Override
        public boolean hasNext() {

            return getS3ObjectIterator().hasNext();
        }

        @Override
        public Document next() {

            S3Object object = getS3ObjectIterator().next();
            // Skip objects that represent folders based on size
            if (object.size() == 0) {

                LOGGER.info("Skipping virtual folder: " + object.key());
                return null;
            }

            LOGGER.debug("AWS S3 Object Key: " + object.key());
            Document document;
            try {
                document = getLoader().loadDocument(getAWSS3Bucket(), object.key(), documentParser);
            } catch(BlankDocumentException bde) {

                LOGGER.warn(String.format("BlankDocumentException: Error while parsing document %s.", contextPath));
                throw bde;
            } catch (Exception e) {

                throw new ModuleException(
                    String.format("Error while parsing document %s.", contextPath),
                    MuleVectorsErrorType.DOCUMENT_PARSING_FAILURE,
                    e);
            }
            MetadataUtils.addMetadataToDocument(document, fileType, object.key());
            return document;
        }
    }

    public class MediaIterator extends BaseStorage.MediaIterator {

        @Override
        public boolean hasNext() {

            return getS3ObjectIterator().hasNext();
        }

        @Override
        public Media next() {

            S3Object object = getS3ObjectIterator().next();

            // Skip objects that represent folders based on size
            if (object.size() == 0) {

                LOGGER.info("Skipping virtual folder: " + object.key());
                return null;
            }

            LOGGER.debug("AWS S3 Object Key: " + object.key());
            Media media;
            try {

                media = Media.fromImage(loadImage(getAWSS3Bucket(), object.key()));
                MetadataUtils.addImageMetadataToMedia(media, mediaType);

            } catch (Exception e) {
                throw new ModuleException(
                    String.format("Error while loading media %s.", contextPath),
                    MuleVectorsErrorType.MEDIA_OPERATIONS_FAILURE,
                    e);
            }
            return media;
        }
    }
}
