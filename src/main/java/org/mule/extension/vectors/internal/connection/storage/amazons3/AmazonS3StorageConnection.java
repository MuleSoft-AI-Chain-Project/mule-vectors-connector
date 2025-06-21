package org.mule.extension.vectors.internal.connection.storage.amazons3;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

public class AmazonS3StorageConnection implements BaseStorageConnection {

  private String awsRegion;
  private String awsAccessKeyId;
  private String awsSecretAccessKey;
  private S3Client s3Client;

  public AmazonS3StorageConnection(String awsRegion, String awsAccessKeyId, String awsSecretAccessKey) {
    this.awsRegion = awsRegion;
    this.awsAccessKeyId = awsAccessKeyId;
    this.awsSecretAccessKey = awsSecretAccessKey;
  }

  public String getAwsRegion() {
    return awsRegion;
  }

  public String getAwsAccessKeyId() {
    return awsAccessKeyId;
  }

  public String getAwsSecretAccessKey() {
    return awsSecretAccessKey;
  }

  public S3Client getS3Client() {
    return s3Client;
  }

  @Override
  public String getStorageType() {
    return Constants.STORAGE_TYPE_AWS_S3;
  }

  @Override
  public void connect() {

    this.s3Client = S3Client.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)))
        .build();
  }

  @Override
  public void disconnect() {

    if(this.s3Client != null) {

      this.s3Client.close();
    }
  }

  @Override
  public boolean isValid() {

    this.s3Client.listBuckets();
    return true;
  }

  public InputStream loadFile(String bucket, String key) {

    try {

      if (bucket == null) {
        throw new IllegalArgumentException("Bucket must not be null");
      }
      if (key == null) {
        throw new IllegalArgumentException("Key must not be null");
      }

      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build();

      return s3Client.getObject(getObjectRequest);

    } catch (S3Exception e) {
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load document", e);
    }
  }
}
