package org.mule.extension.vectors.internal.connection.storage.azureblob;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("azureBlob")
@DisplayName("Azure Blob")
@ExternalLib(name = "Azure Blob Storage",
    type=DEPENDENCY,
    description = "Azure Blob Storage",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "com.azure.storage.blob.BlobClient",
    coordinates = "com.azure:azure-storage-blob:12.30.0")
public class AzureBlobStorageConnectionProvider extends BaseStorageConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private AzureBlobStorageConnectionParameters azureBlobStorageConnectionParameters;

  @Override
  public BaseStorageConnection connect() throws ConnectionException {

    try {

      AzureBlobStorageConnection azureBlobStorageConnection = new AzureBlobStorageConnection(azureBlobStorageConnectionParameters.getAzureName(),
                                                                                             azureBlobStorageConnectionParameters.getAzureKey());
      azureBlobStorageConnection.connect();
      return azureBlobStorageConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Azure Blob.", e);
    }
  }

  @Override
  public void disconnect(BaseStorageConnection connection) {

    connection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(BaseStorageConnection connection) {

    try {

      if (connection.isValid()) {
        return ConnectionValidationResult.success();
      } else {
        return ConnectionValidationResult.failure("Failed to validate connection to Azure Blob", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to Azure Blob", e);
    }
  }
}
