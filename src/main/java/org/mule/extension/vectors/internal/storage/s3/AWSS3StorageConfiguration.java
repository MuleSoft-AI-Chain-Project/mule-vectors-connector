package org.mule.extension.vectors.internal.storage.s3;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.BaseStorageConfiguration;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("awsS3")
@DisplayName("Amazon S3")
public class AWSS3StorageConfiguration implements BaseStorageConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("us-east-2")
  private String awsRegion;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("<your-access-key-id>")
  private String awsAccessKeyId;

  @Parameter
  @Password
  @Placement(order = 3)
  @Example("<your-secret-access-key>")
  private String awsSecretAccessKey;

  @Override
  public String getStorageType() {
    return Constants.STORAGE_TYPE_AWS_S3;
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

  public void setAwsRegion(String awsRegion) {
    this.awsRegion = awsRegion;
  }

  public void setAwsAccessKeyId(String awsAccessKeyId) {
    this.awsAccessKeyId = awsAccessKeyId;
  }

  public void setAwsSecretAccessKey(String awsSecretAccessKey) {
    this.awsSecretAccessKey = awsSecretAccessKey;
  }
}
