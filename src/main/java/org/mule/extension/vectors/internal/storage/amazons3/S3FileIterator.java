package org.mule.extension.vectors.internal.storage.amazons3;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import static java.lang.String.format;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.NoSuchElementException;

public class S3FileIterator implements FileIterator {
    private final AmazonS3Storage s3Client;
    private final String bucket;
    private final String prefix;
    private Iterator<S3Object> s3ObjectIterator;
    private String continuationToken = null;
    private ListObjectsV2Response response;

    public S3FileIterator(AmazonS3Storage s3Client, String bucket, String prefix) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.prefix = prefix;
        this.s3ObjectIterator = null;
    }

    private Iterator<S3Object> getS3ObjectIterator() {
        if (s3ObjectIterator != null && !s3ObjectIterator.hasNext() && continuationToken != null) {
            continuationToken = response.nextContinuationToken();
        }
        if (s3ObjectIterator == null || (!s3ObjectIterator.hasNext() && continuationToken != null)) {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucket);
            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
                requestBuilder.delimiter("/");
            }
            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }
            ListObjectsV2Request listObjectsV2Request = requestBuilder.build();
            response = s3Client.getS3Client().listObjectsV2(listObjectsV2Request);
            s3ObjectIterator = response.contents().stream()
                .filter(obj -> obj.size() > 0) // skip folders
                .iterator();
        }
        return s3ObjectIterator != null ? s3ObjectIterator : Collections.emptyIterator();
    }

    @Override
    public boolean hasNext() {
        return getS3ObjectIterator().hasNext();
    }

    @Override
    public File next() {
        if (!hasNext()) throw new NoSuchElementException();
        S3Object object = getS3ObjectIterator().next();
        ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.loadFile(bucket, object.key());
        HashMap<String, Object> metadata = new HashMap(){{
            put(Constants.METADATA_KEY_SOURCE, format("s3://%s/%s", bucket, object.key()));
        }};
        return new File(responseInputStream, bucket + "/" + object.key(), object.key(), responseInputStream.response().contentType(), metadata);
    }
} 
