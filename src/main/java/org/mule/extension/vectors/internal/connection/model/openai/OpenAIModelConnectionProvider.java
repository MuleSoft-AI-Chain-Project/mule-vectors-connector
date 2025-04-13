package org.mule.extension.vectors.internal.connection.model.openai;

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

@Alias("openAI")
@DisplayName("OpenAI")
public class OpenAIModelConnectionProvider  extends BaseModelConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIModelConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private OpenAIModelConnectionParameters openAIModelConnectionParameters;

  @Override
  public BaseModelConnection connect() throws ConnectionException {

    try {

      OpenAIModelConnection openAIModelConnection =
          new OpenAIModelConnection(openAIModelConnectionParameters.getApiKey());
      openAIModelConnection.connect();
      return openAIModelConnection;

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Open AI", e);
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
        return ConnectionValidationResult.failure("Failed to validate connection to Open AI", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to Open AI", e);
    }
  }
}
