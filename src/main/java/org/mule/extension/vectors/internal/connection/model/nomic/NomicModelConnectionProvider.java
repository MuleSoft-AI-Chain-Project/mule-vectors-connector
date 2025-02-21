package org.mule.extension.vectors.internal.connection.model.nomic;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("nomic")
@DisplayName("Nomic")
public class NomicModelConnectionProvider  extends BaseModelConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(NomicModelConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private NomicModelConnectionParameters nomicModelConnectionParameters;

  @Override
  public BaseModelConnection connect() throws ConnectionException {

    try {

      NomicModelConnection nomicModelConnection = new NomicModelConnection(
          nomicModelConnectionParameters.getApiKey(),
          nomicModelConnectionParameters.getMaxAttempts(),
          nomicModelConnectionParameters.getTotalTimeout());
      nomicModelConnection.connect();
      return nomicModelConnection;

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Nomic", e);
    }
  }

  @Override
  public void disconnect(BaseModelConnection connection) {

    try {

      connection.disconnect();
    } catch (Exception e) {

      LOGGER.error("Failed to close connection", e);
    }
  }

  @Override
  public ConnectionValidationResult validate(BaseModelConnection connection) {

    try {

      if (connection.isValid()) {
        return ConnectionValidationResult.success();
      } else {
        return ConnectionValidationResult.failure("Failed to validate connection to Nomic", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to Nomic", e);
    }
  }
}
