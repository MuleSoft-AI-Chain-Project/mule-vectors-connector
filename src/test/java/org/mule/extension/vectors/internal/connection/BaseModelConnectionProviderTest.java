package org.mule.extension.vectors.internal.connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.api.request.proxy.HttpProxyConfig;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseModelConnectionProviderTest {

  @Mock
  HttpService httpService;
  @Mock
  HttpClient httpClient;
  @Mock
  TlsContextFactory tlsContext;
  @Mock
  HttpProxyConfig proxyConfig;
  @Mock
  HttpClientConfiguration config;

  TestProvider provider;

  static class TestProvider extends BaseModelConnectionProvider {

    @Override
    public BaseModelConnection connect() {
      return null;
    }

    @Override
    public void disconnect(BaseModelConnection c) {}

    @Override
    public org.mule.runtime.api.connection.ConnectionValidationResult validate(BaseModelConnection c) {
      return null;
    }

    // Expose for test
    void setField(String name, Object value) {
      try {
        Field f = BaseModelConnectionProvider.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(this, value);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @BeforeEach
  void setup() {
    provider = new TestProvider();
    provider.setField("configName", "test");
  }

  @Test
  void initialiseCreatesAndStartsHttpClient() {
    // Minimal fake client factory
    when(httpService.getClientFactory()).thenReturn(config1 -> {
      assertNotNull(config1);
      return httpClient;
    });
    provider.setField("httpService", httpService);
    provider.setField("tlsContext", tlsContext);
    provider.setField("proxyConfig", proxyConfig);
    provider.initialise();
    assertEquals(httpClient, provider.getHttpClient());
  }

  @Test
  void disposeStopsAndNullsHttpClient() {
    when(httpService.getClientFactory()).thenReturn(config1 -> httpClient);
    provider.setField("httpService", httpService);
    provider.setField("tlsContext", tlsContext);
    provider.initialise();
    provider.dispose();
    assertNull(provider.getHttpClient());
    // Double dispose should not throw
    provider.dispose();
  }

  @Test
  void createClientConfigurationHandlesNulls() {
    when(httpService.getClientFactory()).thenReturn(config1 -> httpClient);
    provider.setField("httpService", httpService);
    provider.setField("tlsContext", null);
    provider.setField("proxyConfig", null);
    provider.initialise();
    assertEquals(httpClient, provider.getHttpClient());
  }
}
