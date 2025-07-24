package org.mule.extension.vectors.internal.connection.provider.store.elasticsearch;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnectionProvider;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElasticsearchStoreConnectionProviderTest {

  ElasticsearchStoreConnectionProvider provider;
  ElasticsearchStoreConnectionParameters params;

  @BeforeEach
  void setUp() {
    provider = new ElasticsearchStoreConnectionProvider();
    params = new ElasticsearchStoreConnectionParameters();
    try {
      java.lang.reflect.Field urlF = ElasticsearchStoreConnectionParameters.class.getDeclaredField("url");
      urlF.setAccessible(true);
      urlF.set(params, "http://localhost:9200");
      java.lang.reflect.Field pwF = ElasticsearchStoreConnectionParameters.class.getDeclaredField("password");
      pwF.setAccessible(true);
      pwF.set(params, "pw");
    } catch (Exception e) {
      fail(e);
    }
    setParams(provider, params);
  }

  static void setParams(ElasticsearchStoreConnectionProvider provider, ElasticsearchStoreConnectionParameters params) {
    try {
      java.lang.reflect.Field f =
          ElasticsearchStoreConnectionProvider.class.getDeclaredField("elasticsearchStoreConnectionParameters");
      f.setAccessible(true);
      f.set(provider, params);
    } catch (Exception e) {
      fail(e);
    }
  }

  @Test
  void connect_returnsConnection() throws Exception {
    // Use a test double for the connection
    ElasticsearchStoreConnection testConn = new ElasticsearchStoreConnection(params) {

      @Override
      public void initialise() {}

      @Override
      public void disconnect() {}
    };
    java.lang.reflect.Field rcF = ElasticsearchStoreConnection.class.getDeclaredField("restClient");
    rcF.setAccessible(true);
    rcF.set(testConn, RestClient.builder(HttpHost.create("http://localhost:9200")).build());
    java.lang.reflect.Field connF = ElasticsearchStoreConnectionProvider.class.getDeclaredField("elasticsearchStoreConnection");
    connF.setAccessible(true);
    connF.set(provider, testConn);
    BaseStoreConnection conn = provider.connect();
    assertNotNull(conn);
    assertTrue(conn instanceof ElasticsearchStoreConnection);
  }

  @Test
  void dispose_doesNotThrow() throws Exception {
    // Use a test double for the connection
    ElasticsearchStoreConnection testConn = new ElasticsearchStoreConnection(params) {

      @Override
      public void initialise() {}

      @Override
      public void disconnect() {}
    };
    java.lang.reflect.Field rcF = ElasticsearchStoreConnection.class.getDeclaredField("restClient");
    rcF.setAccessible(true);
    rcF.set(testConn, RestClient.builder(HttpHost.create("http://localhost:9200")).build());
    java.lang.reflect.Field connF = ElasticsearchStoreConnectionProvider.class.getDeclaredField("elasticsearchStoreConnection");
    connF.setAccessible(true);
    connF.set(provider, testConn);
    assertDoesNotThrow(provider::dispose);
  }
}
