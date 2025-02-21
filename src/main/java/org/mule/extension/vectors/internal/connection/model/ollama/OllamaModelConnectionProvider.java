package org.mule.extension.vectors.internal.connection.model.ollama;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("ollama")
@DisplayName("Ollama")
public class OllamaModelConnectionProvider extends BaseModelConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(OllamaModelConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private OllamaModelConnectionParameters ollamaModelConnectionParameters;

  @Override
  public BaseModelConnection connect() throws ConnectionException {

    try {

      OllamaModelConnection ollamaModelConnection = new OllamaModelConnection(
          ollamaModelConnectionParameters.getBaseUrl(),
          ollamaModelConnectionParameters.getMaxAttempts(),
          ollamaModelConnectionParameters.getTotalTimeout());
      ollamaModelConnection.connect();
      return ollamaModelConnection;

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Ollama", e);
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
        return ConnectionValidationResult.failure("Failed to validate connection to Ollama", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to Ollama", e);
    }
  }
}
