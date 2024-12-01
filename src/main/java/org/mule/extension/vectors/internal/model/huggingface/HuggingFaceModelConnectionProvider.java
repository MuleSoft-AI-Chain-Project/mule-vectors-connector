package org.mule.extension.vectors.internal.model.huggingface;

import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.extension.vectors.internal.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnectionProvider extends BaseModelConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuggingFaceModelConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private HuggingFaceModelConnectionParameters huggingFaceModelConnectionParameters;

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
