package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudFileIterator;
import com.google.cloud.storage.Blob;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.List;

public class GoogleCloudStorageService implements StorageService {
    private final GoogleCloudStorage gcsClient;

    public GoogleCloudStorageService(GoogleCloudStorage gcsClient) {
        this.gcsClient = gcsClient;
    }

    @Override
    public File getFile(String path) {
        String[] bucketAndObject = GoogleCloudStorage.parseContextPath(path);
        String bucket = bucketAndObject[0];
        String objectName = bucketAndObject[1];
        Blob blob = gcsClient.loadFile(bucket, objectName);
        InputStream content = Channels.newInputStream(blob.reader());
        HashMap<String, Object> metadata = new HashMap(){{
            put(Constants.METADATA_KEY_SOURCE, "gs://" + blob.getBucket() + "/" + blob.getName());
            put("bucket", blob.getBucket());
            put("name", blob.getName());
            put("contentType", blob.getContentType());
            put("size", blob.getSize());
            put("createTime", blob.getCreateTimeOffsetDateTime().toString());
            put("updateTime", blob.getUpdateTimeOffsetDateTime().toString());
        }};
        return new File(content, bucket + "/" + objectName, objectName, blob.getContentType(), metadata);
    }

    @Override
    public FileIterator getFileIterator(String directory) {
        String[] bucketAndPrefix = GoogleCloudStorage.parseContextPath(directory);
        String bucket = bucketAndPrefix[0];
        String prefix = bucketAndPrefix[1];
        List<Blob> objects = gcsClient.listFiles(bucket, prefix);
        return new GoogleCloudFileIterator(gcsClient, bucket, objects);
    }
} 
