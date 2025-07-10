package org.mule.extension.vectors.internal.connection.model.einstein;


import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;

import org.mule.runtime.extension.api.annotation.Alias;

import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("einstein")
@DisplayName("Einstein")
public class EinsteinModelConnectionProvider extends BaseModelConnectionProvider  {

  private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinModelConnectionProvider.class);



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
