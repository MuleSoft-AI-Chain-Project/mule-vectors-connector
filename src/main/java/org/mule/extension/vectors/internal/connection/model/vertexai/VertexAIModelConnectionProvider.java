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
public class VertexAIModelConnectionProvider extends BaseModelConnectionProvider implements Startable, Stoppable {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIModelConnectionProvider.class);
  
  
  private HttpClient httpClient;
  
  @RefName
  private String configName;
  
  @Inject
  private HttpService httpService;
  
  @ParameterGroup(name = CONNECTION)
  private VertexAIModelConnectionParameters vertexAIModelConnectionParameters;
  @Parameter
  @Optional
  @Expression(NOT_SUPPORTED)
  @DisplayName("TLS Configuration")
  @Placement(tab = Placement.SECURITY_TAB)
  private TlsContextFactory tlsContext;
  
  @Parameter
  @Optional
  @Summary("Reusable configuration element for outbound connections through a proxy")
  @Placement(tab = "Proxy")
  private HttpProxyConfig proxyConfig;
  
  @Override
  public BaseModelConnection connect() throws ConnectionException {
    try {
      LOGGER.info("Connecting to Google Vertex AI with parameters: {}", vertexAIModelConnectionParameters);
      VertexAIModelConnection vertexAIModelConnection = new VertexAIModelConnection(
      vertexAIModelConnectionParameters.getProjectId(),
      vertexAIModelConnectionParameters.getLocation(),
      vertexAIModelConnectionParameters.getClientEmail(),
      vertexAIModelConnectionParameters.getClientId(),
      vertexAIModelConnectionParameters.getPrivateKeyId(),
      vertexAIModelConnectionParameters.getPrivateKey(),
      vertexAIModelConnectionParameters.getTotalTimeout(),
      httpClient
      );
      vertexAIModelConnection.connect();
      return vertexAIModelConnection;
    } catch (ConnectionException e) {
      throw e;
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Google Cloud Storage.", e);
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
        return ConnectionValidationResult.failure("Failed to validate connection to Google Cloud Storage", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to Google Cloud Storage", e);
    }
  }
  
  @Override
  public void start() throws MuleException {
    HttpClientConfiguration config = createClientConfiguration();
    httpClient = httpService.getClientFactory().create(config);
    httpClient.start();
  }
  
  private HttpClientConfiguration createClientConfiguration() {
    HttpClientConfiguration.Builder builder = new HttpClientConfiguration.Builder()
    .setName(configName);
    if (null != tlsContext) {
      builder.setTlsContextFactory(tlsContext);
    } else {
      builder.setTlsContextFactory(TlsContextFactory.builder().buildDefault());
    }
    if (proxyConfig != null) {
      builder.setProxyConfig(proxyConfig);
    }
    return builder.build();
  }
  
  @Override
  public void stop() throws MuleException {
    if (httpClient != null) {
      httpClient.stop();
      httpClient = null;
    }
  }
}
