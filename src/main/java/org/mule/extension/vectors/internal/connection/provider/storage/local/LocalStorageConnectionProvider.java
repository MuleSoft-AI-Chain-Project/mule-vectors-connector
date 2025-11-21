package org.mule.extension.vectors.internal.connection.provider.storage.local;

import org.mule.extension.vectors.internal.connection.provider.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.provider.storage.BaseStorageConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("local")
@DisplayName("Local")
public class LocalStorageConnectionProvider implements BaseStorageConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private LocalStorageConnectionParameters localStorageConnectionParameters;
  private LocalStorageConnection localStorageConnection;

  @Override
  public BaseStorageConnection connect() throws ConnectionException {


    return localStorageConnection;


  }


  @Override
  public void dispose() {
    localStorageConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
    localStorageConnection = new LocalStorageConnection(localStorageConnectionParameters.getWorkingDir());
    localStorageConnection.initialise();
  }
}
