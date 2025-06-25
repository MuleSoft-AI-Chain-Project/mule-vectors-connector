package org.mule.extension.vectors.internal.storage.amazons3;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;

public class AmazonS3Storage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3Storage.class);
    public static final String S_3 = "s3://";
    public static final String S_4 = "S3://";
    public static final String BACK_SLASH = "/";


    private final S3Client s3Client;

    public AmazonS3Storage(StorageConfiguration storageConfiguration, AmazonS3StorageConnection amazonS3StorageConnection) {
        this.s3Client = amazonS3StorageConnection.getS3Client();
    }

    public InputStream loadFile(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }
    public static String parseBucket(String s3Url) {
        if (s3Url.startsWith(S_3) || s3Url.startsWith(S_4)) {
            s3Url = s3Url.substring(5);
        }
        return s3Url.contains(BACK_SLASH) ? s3Url.substring(0, s3Url.indexOf(BACK_SLASH)) : s3Url;
    }

    public static String parseKey(String s3Url) {
        if (s3Url.startsWith(S_3) || s3Url.startsWith(S_4)) {
            s3Url = s3Url.substring(5);
        }
        int slashIndex = s3Url.indexOf(BACK_SLASH);
        return slashIndex != -1 ? s3Url.substring(slashIndex + 1) : "";
    }

    public S3Client getS3Client() {
        return s3Client;
    }
}
