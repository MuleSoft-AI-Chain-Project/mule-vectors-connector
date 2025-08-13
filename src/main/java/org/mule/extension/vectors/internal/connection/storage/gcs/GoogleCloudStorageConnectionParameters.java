package org.mule.extension.vectors.internal.connection.storage.gcs;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class GoogleCloudStorageConnectionParameters extends BaseStorageConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("<your-project-id>")
  private String projectId;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("<your-client-email>")
  private String clientEmail;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  @Example("<your-client-id>")
  private String clientId;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4)
  @Example("<your-private-key-id>")
  private String privateKeyId;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 5)
  @Example("<your-private-key>")
  private String privateKey;

  public String getProjectId() {
    return projectId;
  }

  public String getClientEmail() {
    return clientEmail;
  }

  public String getClientId() {
    return clientId;
  }

  public String getPrivateKeyId() {
    return privateKeyId;
  }

  public String getPrivateKey() {
    return privateKey;
  }
}
