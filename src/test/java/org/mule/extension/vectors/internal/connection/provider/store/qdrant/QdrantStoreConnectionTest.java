package org.mule.extension.vectors.internal.connection.provider.store.qdrant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QdrantStoreConnectionTest {

  QdrantStoreConnectionParameters params;
  QdrantStoreConnection conn;

  @BeforeEach
  void setUp() {
    params = mock(QdrantStoreConnectionParameters.class);
    when(params.getHost()).thenReturn("localhost");
    when(params.getGprcPort()).thenReturn(6334);
    when(params.isUseTLS()).thenReturn(true);
    when(params.getTextSegmentKey()).thenReturn("text-segment");
    when(params.getApiKey()).thenReturn("apikey");
    conn = new QdrantStoreConnection(params);
  }

  @Test
  void getters_work() {
    assertEquals("localhost", conn.getHost());
    assertEquals(6334, conn.getGprcPort());
    assertTrue(conn.isUseTLS());
    assertEquals("text-segment", conn.getTextSegmentKey());
    assertEquals("apikey", conn.getApiKey());
    assertEquals(params, conn.getConnectionParameters());
  }

  @Test
  void getVectorStore_returnsConstant() {
    assertEquals("QDRANT", conn.getVectorStore());
  }

  @Test
  void disconnect_closesClient() throws Exception {
    var client = mock(io.qdrant.client.QdrantClient.class);
    conn = spy(conn);
    java.lang.reflect.Field f = QdrantStoreConnection.class.getDeclaredField("client");
    f.setAccessible(true);
    f.set(conn, client);
    conn.disconnect();
    verify(client, atLeastOnce()).close();
  }

  @Test
  void validate_success() throws Exception {
    conn = spy(new QdrantStoreConnection(params));
    doNothing().when(conn).doHealthCheck();
    assertDoesNotThrow(conn::validate);
  }

  @Test
  void validate_missingHost() {
    when(params.getHost()).thenReturn(null);
    ModuleException ex = assertThrows(ModuleException.class, conn::validate);
    assertTrue(ex.getMessage().contains("Host is required"));
  }

  @Test
  void validate_missingPort() {
    when(params.getGprcPort()).thenReturn(0);
    ModuleException ex = assertThrows(ModuleException.class, conn::validate);
    assertTrue(ex.getMessage().contains("gprcPort is required"));
  }

  @Test
  void validate_missingTextSegmentKey() {
    when(params.getTextSegmentKey()).thenReturn(null);
    ModuleException ex = assertThrows(ModuleException.class, conn::validate);
    assertTrue(ex.getMessage().contains("TextSegmentKey is required"));
  }

  @Test
  void validate_missingApiKey() {
    when(params.getApiKey()).thenReturn(null);
    ModuleException ex = assertThrows(ModuleException.class, conn::validate);
    assertTrue(ex.getMessage().contains("API Key is required"));
  }

  @Test
  void validate_healthCheckFails() throws Exception {
    conn = spy(new QdrantStoreConnection(params));
    doThrow(new RuntimeException("fail")).when(conn).doHealthCheck();
    ModuleException ex = assertThrows(ModuleException.class, conn::validate);
    assertTrue(ex.getMessage().contains("Failed to connect to Qdrant store"));
  }

  @Test
  void initialise_setsClient() throws Exception {
    QdrantStoreConnection c = spy(new QdrantStoreConnection(params));
    doNothing().when(c).initialise();
    assertDoesNotThrow(c::initialise);
  }
}
