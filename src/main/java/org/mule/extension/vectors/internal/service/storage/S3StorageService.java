package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.storage.amazons3.AmazonS3Storage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.amazons3.S3FileIterator;
import software.amazon.awssdk.services.s3.model.S3Object;
import java.io.InputStream;
import java.util.List;

public class S3StorageService implements StorageService {
    private final AmazonS3Storage s3Client;

    public S3StorageService(AmazonS3Storage s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public File getFile(String path) {
        String bucket = AmazonS3Storage.parseBucket(path);
        String key = AmazonS3Storage.parseKey(path);
        InputStream content = s3Client.loadFile(bucket, key);
        return new File(content, bucket + "/" + key, key);
    }

    @Override
    public FileIterator getFileIterator(String contextPath) {
        String bucket = AmazonS3Storage.parseBucket(contextPath);
        String prefix = AmazonS3Storage.parseKey(contextPath);
        return new S3FileIterator(s3Client, bucket, prefix);
    }

} 
