package org.mule.extension.vectors.internal.connection.model.ollama;


import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("ollama")
@DisplayName("Ollama")
public class OllamaModelConnectionProvider extends BaseModelConnectionProvider  {

  private static final Logger LOGGER = LoggerFactory.getLogger(OllamaModelConnectionProvider.class);

 
  @ParameterGroup(name = CONNECTION)
  private OllamaModelConnectionParameters ollamaModelConnectionParameters;

 
  @Override
  public BaseModelConnection connect() throws ConnectionException {

      OllamaModelConnection ollamaModelConnection = new OllamaModelConnection(
          ollamaModelConnectionParameters.getBaseUrl(),
          ollamaModelConnectionParameters.getTotalTimeout(),
          getHttpClient());


      return ollamaModelConnection;
    }
}
