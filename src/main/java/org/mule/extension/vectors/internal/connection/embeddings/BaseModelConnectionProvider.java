package org.mule.extension.vectors.internal.connection.embeddings;


import org.apache.commons.lang3.StringUtils;
import org.mule.extension.vectors.api.request.proxy.HttpProxyConfig;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;

import javax.inject.Inject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

public abstract class BaseModelConnectionProvider implements CachedConnectionProvider<BaseModelConnection>,
    Initialisable,
    Disposable {
  private HttpClient httpClient;
  @Inject
  private HttpService httpService;

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
  @RefName
  private String configName;
  @Override
  public void initialise() {
    HttpClientConfiguration config = createClientConfiguration();
    httpClient = httpService.getClientFactory().create(config);
    httpClient.start();
  }

  public HttpClient getHttpClient() {
    return httpClient;
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
  public void dispose() {
    if (httpClient != null) {
      httpClient.stop();
      httpClient = null;
    }
  }
  @Override
  public ConnectionValidationResult validate(BaseModelConnection connection) {
    try {
      connection.validate();
      return ConnectionValidationResult.success();
    } catch (Exception e) {
      return ConnectionValidationResult.failure(e.getMessage(), e);
    }
  }

  @Override
  public void disconnect(BaseModelConnection connection) {
    connection.disconnect();
  }

}
