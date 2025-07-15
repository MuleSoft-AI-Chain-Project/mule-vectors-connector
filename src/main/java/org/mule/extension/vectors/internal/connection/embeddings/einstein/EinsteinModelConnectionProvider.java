package org.mule.extension.vectors.internal.connection.embeddings.einstein;


import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;

import org.mule.runtime.extension.api.annotation.Alias;

import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("einstein")
@DisplayName("Einstein")
public class EinsteinModelConnectionProvider extends BaseModelConnectionProvider  {


  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private EinsteinModelConnectionParameters einsteinModelConnectionParameters;


  @Override
  public BaseModelConnection connect() throws ConnectionException {
    try {
      EinsteinModelConnection einsteinModelConnection = new EinsteinModelConnection(
          einsteinModelConnectionParameters.getSalesforceOrg(),
          einsteinModelConnectionParameters.getClientId(),
          einsteinModelConnectionParameters.getClientSecret(),
          getHttpClient());
      return einsteinModelConnection;
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Einstein", e);
    }
  }

}
