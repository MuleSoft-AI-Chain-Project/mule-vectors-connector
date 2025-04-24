package org.mule.extension.vectors.api.request.proxy;

import org.mule.runtime.http.api.client.proxy.ProxyConfig;

/**
 * Marker interface for exposing the proxy configuration as an imported type.
 */
public interface HttpProxyConfig extends ProxyConfig {

  interface HttpNtlmProxyConfig extends HttpProxyConfig, NtlmProxyConfig {
  }
}
