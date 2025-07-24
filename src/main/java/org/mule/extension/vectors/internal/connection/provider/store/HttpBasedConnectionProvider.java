package org.mule.extension.vectors.internal.connection.provider.store;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import static java.util.Optional.ofNullable;

import org.mule.extension.vectors.api.request.proxy.HttpProxyConfig;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;

import javax.inject.Inject;

public abstract class HttpBasedConnectionProvider implements BaseStoreConnectionProvider {

  @RefName
  private String configName;

  @Parameter
  @Optional
  @Placement(tab = "Proxy", order = 3)
  @Expression(NOT_SUPPORTED)
  @DisplayName("Proxy Configuration")
  private HttpProxyConfig proxyConfig;

  @Expression(NOT_SUPPORTED)
  @Placement(tab = "Security", order = 1)
  @Parameter
  @Optional
  @DisplayName("TLS Configuration")
  private TlsContextFactory tlsContextFactory;

  @Inject
  private HttpService httpService;

  private HttpClient httpClient;

  @Override
  public void initialise() throws InitialisationException {

    initialiseIfNeeded(tlsContextFactory);



    httpClient = httpService.getClientFactory().create(createClientConfiguration());
    httpClient.start();

  }

  @Override
  public void dispose() {

    ofNullable(httpClient).ifPresent(HttpClient::stop);
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }


  public HttpProxyConfig getProxyConfig() {
    return proxyConfig;
  }

  public TlsContextFactory getTlsContextFactory() {
    return tlsContextFactory;
  }

  private HttpClientConfiguration createClientConfiguration() {

    return new HttpClientConfiguration.Builder().setName(configName)
        .setTlsContextFactory(tlsContextFactory)
        .setProxyConfig(proxyConfig)
        .build();
  }
}
