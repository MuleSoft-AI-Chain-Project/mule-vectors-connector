package org.mule.extension.vectors.internal.storage.amazons3;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

public class S3FileIterator implements FileIterator {
    private final AmazonS3Storage s3Client;
    private final String bucket;
    private final Iterator<S3Object> s3ObjectIterator;

    public S3FileIterator(AmazonS3Storage s3Client, String bucket, List<S3Object> objects) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.s3ObjectIterator = objects.iterator();
    }

    @Override
    public boolean hasNext() {
        return s3ObjectIterator.hasNext();
    }

    @Override
    public File next() {
        while (s3ObjectIterator.hasNext()) {
            S3Object object = s3ObjectIterator.next();
            if (object.size() == 0) {
                // Skip virtual folders
                continue;
            }
            ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.loadFile(bucket, object.key());
            HashMap<String, Object> metadata = new HashMap(){{
                put(Constants.METADATA_KEY_SOURCE, format("s3://%s/%s", bucket, object.key()));
            }};
            return new File(responseInputStream, bucket + "/" + object.key(), object.key(), responseInputStream.response().contentType(), metadata);
        }
        return null;
    }
} 
