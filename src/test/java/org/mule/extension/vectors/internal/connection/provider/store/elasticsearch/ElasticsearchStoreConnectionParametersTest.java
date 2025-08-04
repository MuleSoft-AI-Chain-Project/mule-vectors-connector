package org.mule.extension.vectors.internal.connection.provider.store.elasticsearch;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnectionParameters;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class ElasticsearchStoreConnectionParametersTest {

  private ElasticsearchStoreConnectionParameters buildParams(String url, String user, String password, String apiKey)
      throws Exception {
    ElasticsearchStoreConnectionParameters params = new ElasticsearchStoreConnectionParameters();
    setField(params, "url", url);
    setField(params, "user", user);
    setField(params, "password", password);
    setField(params, "apiKey", apiKey);
    return params;
  }

  private void setField(Object obj, String field, Object value) throws Exception {
    Field f = obj.getClass().getDeclaredField(field);
    f.setAccessible(true);
    f.set(obj, value);
  }

  @Test
  void gettersReturnSetValues() throws Exception {
    ElasticsearchStoreConnectionParameters params = buildParams("http://localhost:9200", "elastic", "pass", "key");
    assertEquals("http://localhost:9200", params.getUrl());
    assertEquals("elastic", params.getUser());
    assertEquals("pass", params.getPassword());
    assertEquals("key", params.getApiKey());
  }

  @Test
  void gettersReturnNullWhenUnset() throws Exception {
    ElasticsearchStoreConnectionParameters params = buildParams(null, null, null, null);
    assertNull(params.getUrl());
    assertNull(params.getUser());
    assertNull(params.getPassword());
    assertNull(params.getApiKey());
  }

  @Test
  void gettersReturnEmptyString() throws Exception {
    ElasticsearchStoreConnectionParameters params = buildParams("", "", "", "");
    assertEquals("", params.getUrl());
    assertEquals("", params.getUser());
    assertEquals("", params.getPassword());
    assertEquals("", params.getApiKey());
  }

  @Test
  void getters_returnValues() throws Exception {
    ElasticsearchStoreConnectionParameters params = new ElasticsearchStoreConnectionParameters();
    java.lang.reflect.Field urlF = ElasticsearchStoreConnectionParameters.class.getDeclaredField("url");
    urlF.setAccessible(true);
    urlF.set(params, "http://localhost:9200");
    java.lang.reflect.Field userF = ElasticsearchStoreConnectionParameters.class.getDeclaredField("user");
    userF.setAccessible(true);
    userF.set(params, "elastic");
    java.lang.reflect.Field pwF = ElasticsearchStoreConnectionParameters.class.getDeclaredField("password");
    pwF.setAccessible(true);
    pwF.set(params, "pw");
    java.lang.reflect.Field apiF = ElasticsearchStoreConnectionParameters.class.getDeclaredField("apiKey");
    apiF.setAccessible(true);
    apiF.set(params, "apikey");
    assertEquals("http://localhost:9200", params.getUrl());
    assertEquals("elastic", params.getUser());
    assertEquals("pw", params.getPassword());
    assertEquals("apikey", params.getApiKey());
  }

  @Test
  void getters_nullAndDefaults() {
    ElasticsearchStoreConnectionParameters params = new ElasticsearchStoreConnectionParameters();
    assertNull(params.getUrl());
    assertNull(params.getUser());
    assertNull(params.getPassword());
    assertNull(params.getApiKey());
  }
}
