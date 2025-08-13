package org.mule.extension.vectors.internal.connection.storage.amazons3;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;

import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("amazonS3")
@DisplayName("Amazon S3")
@ExternalLib(name = "Amazon AWS SDK",
    type=DEPENDENCY,
    description = "Amazon AWS SDK",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "software.amazon.awssdk.services.s3.S3Client",
    coordinates = "software.amazon.awssdk:s3:2.31.6")
public class AmazonS3StorageConnectionProvider implements BaseStorageConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private AmazonS3StorageConnectionParameters amazonS3StorageConnectionParameters;
  private AmazonS3StorageConnection amazonS3StorageConnection;

  @Override
  public BaseStorageConnection connect() throws ConnectionException {
      return amazonS3StorageConnection;
  }

  @Override
  public void disconnect(BaseStorageConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(BaseStorageConnection connection) {
    try {
      connection.validate();
      return ConnectionValidationResult.success();
    } catch (Exception e) {
      return ConnectionValidationResult.failure(e.getMessage(), e);
    }
  }

  @Override
  public void dispose() {
    amazonS3StorageConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
    amazonS3StorageConnection = new AmazonS3StorageConnection(amazonS3StorageConnectionParameters.getAwsRegion(),
                                                                                        amazonS3StorageConnectionParameters.getAwsAccessKeyId(),
                                                                                        amazonS3StorageConnectionParameters.getAwsSecretAccessKey());
    amazonS3StorageConnection.initialise();
  }
}
