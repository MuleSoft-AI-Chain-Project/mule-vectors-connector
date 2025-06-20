package org.mule.extension.vectors.internal.connection.model.vertexai;

import org.mule.extension.vectors.api.request.proxy.HttpProxyConfig;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.tls.TlsContextFactory;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

import javax.inject.Inject;

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
