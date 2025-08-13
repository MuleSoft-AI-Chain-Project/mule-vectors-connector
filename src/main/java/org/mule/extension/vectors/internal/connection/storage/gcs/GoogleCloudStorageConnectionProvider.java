package org.mule.extension.vectors.internal.connection.storage.gcs;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

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

import java.io.IOException;

@Alias("googleCloudStorage")
@DisplayName("Google Cloud Storage")
@ExternalLib(name = "Google Cloud Storage",
    type = DEPENDENCY,
    description = "Google Cloud Storage",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "com.google.cloud.storage.Storage",
    coordinates = "com.google.cloud:google-cloud-storage:2.43.0")
public class GoogleCloudStorageConnectionProvider implements BaseStorageConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private GoogleCloudStorageConnectionParameters googleCloudStorageConnectionParameters;
  private GoogleCloudStorageConnection googleCloudStorageConnection;

  @Override
  public BaseStorageConnection connect() throws ConnectionException {


    return googleCloudStorageConnection;

  }

  @Override
  public void dispose() {
    googleCloudStorageConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
    try {
      googleCloudStorageConnection = new GoogleCloudStorageConnection(
                                                                      googleCloudStorageConnectionParameters.getProjectId(),
                                                                      googleCloudStorageConnectionParameters.getClientEmail(),
                                                                      googleCloudStorageConnectionParameters.getClientId(),
                                                                      googleCloudStorageConnectionParameters.getPrivateKeyId(),
                                                                      googleCloudStorageConnectionParameters.getPrivateKey());
      googleCloudStorageConnection.initialise();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
