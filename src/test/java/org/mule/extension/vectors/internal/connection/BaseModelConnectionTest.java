package org.mule.extension.vectors.internal.connection;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;

import org.junit.jupiter.api.Test;

class BaseModelConnectionTest {

  static class DummyModelConnection implements BaseModelConnection {

    private final String service;

    DummyModelConnection(String service) {
      this.service = service;
    }

    @Override
    public String getEmbeddingModelService() {
      return service;
    }

    @Override
    public void disconnect() {}

    @Override
    public void validate() {}
  }

  @Test
  void shouldReturnEmbeddingModelService() {
    String expected = "test-service";
    BaseModelConnection conn = new DummyModelConnection(expected);
    assertEquals(expected, conn.getEmbeddingModelService());
  }

  @Test
  void shouldImplementBaseModelConnection() {
    BaseModelConnection conn = new DummyModelConnection("svc");
    assertTrue(conn instanceof BaseModelConnection);
  }
}
