package org.mule.extension.vectors.internal.model.mistralai;

import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.extension.vectors.internal.model.BaseModelConnectionProvider;
import org.mule.extension.vectors.internal.model.azureopenai.AzureOpenAIModelConnectionParameters;
import org.mule.extension.vectors.internal.model.huggingface.HuggingFaceModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("mistralAI")
@DisplayName("Mistral AI")
public class MistralAIModelConnectionProvider  extends BaseModelConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MistralAIModelConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private MistralAIModelConnectionParameters mistralAIModelConnectionParameters;

  @Override
  public BaseModelConnection connect() throws ConnectionException {

    throw new ConnectionException("Failed to connect", null);
  }

  @Override
  public void disconnect(BaseModelConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(BaseModelConnection connection) {

    return ConnectionValidationResult.failure("Failed to validate connection", null);
  }
}
