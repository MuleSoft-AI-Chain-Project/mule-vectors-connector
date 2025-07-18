package org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai;


import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("googleVertexAI")
@DisplayName("Google Vertex AI")
public class VertexAIModelConnectionProvider extends BaseModelConnectionProvider  {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIModelConnectionProvider.class);
  

  
  @ParameterGroup(name = CONNECTION)
  private VertexAIModelConnectionParameters vertexAIModelConnectionParameters;
  @Override
  public BaseModelConnection connect() throws ConnectionException {

      LOGGER.info("Connecting to Google Vertex AI with parameters: {}", vertexAIModelConnectionParameters);
      VertexAIModelConnection vertexAIModelConnection = new VertexAIModelConnection(
      vertexAIModelConnectionParameters.getProjectId(),
      vertexAIModelConnectionParameters.getLocation(),
      vertexAIModelConnectionParameters.getClientEmail(),
      vertexAIModelConnectionParameters.getClientId(),
      vertexAIModelConnectionParameters.getPrivateKeyId(),
      vertexAIModelConnectionParameters.getPrivateKey(),
      vertexAIModelConnectionParameters.getTotalTimeout(),
      vertexAIModelConnectionParameters.getBatchSize(),
      getHttpClient()
      );
      return vertexAIModelConnection;

  }

}
