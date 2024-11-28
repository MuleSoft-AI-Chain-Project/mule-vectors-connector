package org.mule.extension.vectors.internal.model;

import org.mule.extension.vectors.internal.model.einstein.EinsteinModelConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseModelConnectionProvider implements PoolingConnectionProvider<BaseModelConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(BaseModelConnectionProvider.class);

  @Parameter
  @Alias("modelConnection")
  @DisplayName("Embedding Model Service")
  @Summary("The embedding model service connection.")
  private BaseModelConnection modelConnection;

  @Override
  public BaseModelConnection connect() throws ConnectionException {

    return this.modelConnection.connect();
  }

  @Override
  public void disconnect(BaseModelConnection connection) {
    connection.disconnect(connection);
  }

  @Override
  public ConnectionValidationResult validate(BaseModelConnection connection) {

    return connection.validate(connection);
  }
}
