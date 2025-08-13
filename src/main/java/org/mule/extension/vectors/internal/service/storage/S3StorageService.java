package org.mule.extension.vectors.internal.service.storage;

import static java.lang.String.format;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.storage.amazons3.AmazonS3Storage;
import org.mule.extension.vectors.internal.storage.amazons3.S3FileIterator;

import java.io.InputStream;
import java.util.HashMap;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3StorageService implements StorageService {

  private final AmazonS3Storage s3Client;

  public S3StorageService(AmazonS3Storage s3Client) {
    this.s3Client = s3Client;
  }

  @Override
  public FileInfo getFile(String path) {
    String bucket = AmazonS3Storage.parseBucket(path);
    String key = AmazonS3Storage.parseKey(path);
    ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.loadFile(bucket, key);
    HashMap<String, Object> metadata = new HashMap() {

      {
        put(Constants.METADATA_KEY_SOURCE, format("s3://%s/%s", bucket, key));
      }
    };
    return new FileInfo(responseInputStream, bucket + "/" + key, key, responseInputStream.response().contentType(), metadata);
  }

  @Override
  public FileIterator getFileIterator(String contextPath) {
    String bucket = AmazonS3Storage.parseBucket(contextPath);
    String prefix = AmazonS3Storage.parseKey(contextPath);
    return new S3FileIterator(s3Client, bucket, prefix);
  }

}
