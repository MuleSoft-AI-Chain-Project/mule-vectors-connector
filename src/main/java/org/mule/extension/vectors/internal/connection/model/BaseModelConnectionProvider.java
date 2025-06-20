package org.mule.extension.vectors.internal.connection.model;

import com.mulesoft.connectors.commons.template.connection.provider.ConnectorConnectionProvider;
import org.mule.extension.vectors.api.request.proxy.HttpProxyConfig;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
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
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;

import javax.inject.Inject;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

public abstract class BaseModelConnectionProvider implements CachedConnectionProvider<BaseModelConnection>,
    ConnectorConnectionProvider<BaseModelConnection>, Initialisable,
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
}
