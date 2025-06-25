package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudFileIterator;
import com.google.cloud.storage.Blob;
import java.io.InputStream;
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
        String objectKey = bucketAndObject[1];
        InputStream content = gcsClient.loadFile(bucket, objectKey);
        System.out.println( "GCP content: " + content+":: "+ bucket + "/" + objectKey + "::"+ objectKey + "::");
        return new File(content, bucket + "/" + objectKey, objectKey);
    }

    @Override
    public FileIterator getFileIterator(String directory) {
        String[] bucketAndPrefix = GoogleCloudStorage.parseContextPath(directory);
        String bucket = bucketAndPrefix[0];
        String prefix = bucketAndPrefix[1];
        return new GoogleCloudFileIterator(gcsClient, bucket, prefix);
    }
} 
