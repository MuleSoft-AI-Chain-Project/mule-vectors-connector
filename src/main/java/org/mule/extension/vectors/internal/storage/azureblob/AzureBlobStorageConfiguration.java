package org.mule.extension.vectors.internal.storage.azureblob;

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

@Alias("azureBlob")
@DisplayName("Azure Blob")
public class AzureBlobStorageConfiguration implements BaseStorageConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("<your-account-name>")
  private String azureName;

  @Parameter
  @Password
  @Placement(order = 2)
  @Example("<your-account-key>")
  private String azureKey;

  @Override
  public String getStorageType() { return Constants.STORAGE_TYPE_AZURE_BLOB; }

  public String getAzureName() {
    return azureName;
  }

  public String getAzureKey() {
    return azureKey;
  }

  public void setAzureName(String azureName) {
    this.azureName = azureName;
  }

  public void setAzureKey(String azureKey) {
    this.azureKey = azureKey;
  }
}
