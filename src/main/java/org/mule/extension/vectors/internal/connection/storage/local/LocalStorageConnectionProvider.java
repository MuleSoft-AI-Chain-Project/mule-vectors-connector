package org.mule.extension.vectors.internal.connection.storage.local;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("local")
@DisplayName("Local")
public class LocalStorageConnectionProvider extends BaseStorageConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private LocalStorageConnectionParameters localStorageConnectionParameters;

  @Override
  public BaseStorageConnection connect() throws ConnectionException {
    try {

      LocalStorageConnection localStorageConnection = new LocalStorageConnection(localStorageConnectionParameters.getWorkingDir());
      localStorageConnection.connect();
      return localStorageConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to access Local File System.", e);
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
        return ConnectionValidationResult.failure("Failed to validate access to Local File System", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate access to Local File System", e);
    }
  }
}
