package org.mule.extension.vectors.internal.connection.storage.amazons3;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static java.lang.String.format;

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

  public Document loadDocument(String bucket, String key, DocumentParser parser) {
    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(ensureNotBlank(bucket, "bucket"))
          .key(ensureNotBlank(key, "key"))
          .build();
      ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(getObjectRequest);
      Document document = parser.parse(inputStream);
      document.metadata().put(Constants.METADATA_KEY_SOURCE, format("s3://%s/%s", bucket, key));
      return document;

    } catch (S3Exception e) {
      throw new RuntimeException(e);
    } catch (BlankDocumentException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load document", e);
    }
  }
}
