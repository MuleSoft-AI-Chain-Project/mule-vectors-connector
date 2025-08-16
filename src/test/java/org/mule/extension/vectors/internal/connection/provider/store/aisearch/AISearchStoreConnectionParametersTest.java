package org.mule.extension.vectors.internal.connection.provider.store.aisearch;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionParameters;

import org.junit.jupiter.api.Test;

class AISearchStoreConnectionParametersTest {

  @Test
  void getters_returnValues() {
    AISearchStoreConnectionParameters params = new AISearchStoreConnectionParameters();
    // Use reflection to set private fields
    try {
      java.lang.reflect.Field urlF = AISearchStoreConnectionParameters.class.getDeclaredField("url");
      urlF.setAccessible(true);
      urlF.set(params, "https://test.search.windows.net");
      java.lang.reflect.Field keyF = AISearchStoreConnectionParameters.class.getDeclaredField("apiKey");
      keyF.setAccessible(true);
      keyF.set(params, "secret");
    } catch (Exception e) {
      fail(e);
    }
    assertEquals("https://test.search.windows.net", params.getUrl());
    assertEquals("secret", params.getApiKey());
  }

  @Test
  void getters_nullByDefault() {
    AISearchStoreConnectionParameters params = new AISearchStoreConnectionParameters();
    assertNull(params.getUrl());
    assertNull(params.getApiKey());
  }
}
