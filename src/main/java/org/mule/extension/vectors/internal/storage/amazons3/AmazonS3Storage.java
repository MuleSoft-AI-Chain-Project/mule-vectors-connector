package org.mule.extension.vectors.internal.storage.amazons3;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.mule.extension.vectors.internal.data.file.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.List;

public class AmazonS3Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3Storage.class);

    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    private final String awsRegion;
    private final S3Client s3Client;

    public AmazonS3Storage(StorageConfiguration storageConfiguration, AmazonS3StorageConnection amazonS3StorageConnection) {
        this.awsAccessKeyId = amazonS3StorageConnection.getAwsAccessKeyId();
        this.awsSecretAccessKey = amazonS3StorageConnection.getAwsSecretAccessKey();
        this.awsRegion = amazonS3StorageConnection.getAwsRegion();
        this.s3Client = amazonS3StorageConnection.getS3Client();
    }

    public InputStream loadFile(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    public List<S3Object> listFiles(String bucket, String prefix) {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucket);
        if (prefix != null && !prefix.isEmpty()) {
            requestBuilder.prefix(prefix);
            requestBuilder.delimiter("/");
        }
        ListObjectsV2Request listObjectsV2Request = requestBuilder.build();
        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);
        return response.contents();
    }

    public static String parseBucket(String s3Url) {
        if (s3Url.startsWith("s3://") || s3Url.startsWith("S3://")) {
            s3Url = s3Url.substring(5);
        }
        return s3Url.contains("/") ? s3Url.substring(0, s3Url.indexOf("/")) : s3Url;
    }

    public static String parseKey(String s3Url) {
        if (s3Url.startsWith("s3://") || s3Url.startsWith("S3://")) {
            s3Url = s3Url.substring(5);
        }
        int slashIndex = s3Url.indexOf("/");
        return slashIndex != -1 ? s3Url.substring(slashIndex + 1) : "";
    }
}
