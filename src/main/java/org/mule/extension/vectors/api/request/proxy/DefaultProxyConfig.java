package org.mule.extension.vectors.api.request.proxy;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Password;

import java.util.Objects;

/**
 * Basic HTTP Proxy configuration based on host and port, and optionally a username and password for proxy authentication.
 *
 * @since 1.0
 */
@Alias("proxy")
@TypeDsl(allowTopLevelDefinition = true)
public class DefaultProxyConfig implements HttpProxyConfig {

  /**
   * Host where the proxy requests will be sent.
   */
  @Parameter
  private String host;

  /**
   * Port where the proxy requests will be sent.
   */
  @Parameter
  private int port = Integer.MAX_VALUE;

  /**
   * The username to authenticate against the proxy.
   */
  @Parameter
  @Optional
  private String username;

  /**
   * The password to authenticate against the proxy.
   */
  @Parameter
  @Optional
  @Password
  private String password;

  /**
   * A list of comma separated hosts against which the proxy should not be used
   */
  @Parameter
  @Optional
  String nonProxyHosts;

  public String getHost() {
    return host;
  }


  public int getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getNonProxyHosts() {
    return nonProxyHosts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultProxyConfig that = (DefaultProxyConfig) o;
    return port == that.port &&
        Objects.equals(host, that.host) &&
        Objects.equals(username, that.username) &&
        Objects.equals(password, that.password) &&
        Objects.equals(nonProxyHosts, that.nonProxyHosts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, username, password, nonProxyHosts);
  }
}
