package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudFileIterator;
import com.google.cloud.storage.Blob;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class GoogleCloudStorageService implements StorageService {
    private final GoogleCloudStorage gcsClient;

    public GoogleCloudStorageService(GoogleCloudStorage gcsClient) {
        this.gcsClient = gcsClient;
    }

    @Override
    public FileInfo getFile(String path) {
        String[] bucketAndObject = GoogleCloudStorage.parseContextPath(path);
        String bucket = bucketAndObject[0];
        String objectKey = bucketAndObject[1];
        InputStream content = gcsClient.loadFile(bucket, objectKey);
        Blob blob = gcsClient.getBlob();
        gcsClient.getStorageService();
        HashMap<String, Object> metadata = new HashMap(){{
            put(Constants.METADATA_KEY_SOURCE, "gs://" + blob.getBucket() + "/" + blob.getName());
            put("bucket", blob.getBucket());
            put("name", blob.getName());
            put("contentType", blob.getContentType());
            put("size", blob.getSize());
            put("createTime", blob.getCreateTimeOffsetDateTime().toString());
            put("updateTime", blob.getUpdateTimeOffsetDateTime().toString());
        }};
        return new FileInfo(content, bucket + "/" + objectKey, objectKey, metadata);
    }

    @Override
    public FileIterator getFileIterator(String contextPath) {
        String[] bucketAndPrefix = GoogleCloudStorage.parseContextPath(contextPath);
        String bucket = bucketAndPrefix[0];
        String prefix = bucketAndPrefix[1];
        return new GoogleCloudFileIterator(gcsClient, bucket, prefix);
    }
} 
