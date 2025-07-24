package org.mule.extension.vectors.internal.connection.provider.embeddings.ollama;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

@Alias("ollama")
@DisplayName("Ollama")
public class OllamaModelConnectionProvider extends BaseModelConnectionProvider {


  @ParameterGroup(name = CONNECTION)
  private OllamaModelConnectionParameters ollamaModelConnectionParameters;


  @Override
  public BaseModelConnection connect() throws ConnectionException {

    OllamaModelConnection ollamaModelConnection = new OllamaModelConnection(
                                                                            ollamaModelConnectionParameters.getBaseUrl(),
                                                                            ollamaModelConnectionParameters.getTimeout(),
                                                                            getHttpClient());


    return ollamaModelConnection;
  }
}
