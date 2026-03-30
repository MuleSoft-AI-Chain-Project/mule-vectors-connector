package org.mule.extension.vectors.internal.connection.provider.store.milvus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.validation.ConnectionValidationStrategies;
import org.mule.runtime.extension.api.exception.ModuleException;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.CheckHealthResponse;
import io.milvus.param.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MilvusStoreConnectionTest {

  @Mock
  MilvusStoreConnectionParameters params;

  MilvusStoreConnection connection;

  @BeforeEach
  void setUp() {
    when(params.getUri()).thenReturn("http://localhost:19530");
    when(params.getHost()).thenReturn("localhost");
    when(params.getPort()).thenReturn(19530);
    when(params.getToken()).thenReturn("token");
    when(params.getUsername()).thenReturn("user");
    when(params.getPassword()).thenReturn("pass");
    when(params.getDatabaseName()).thenReturn("default");
    when(params.getIndexType()).thenReturn("FLAT");
    when(params.getMetricType()).thenReturn("COSINE");
    when(params.getConsistencyLevel()).thenReturn("EVENTUALLY");
    when(params.isAutoFlushOnInsert()).thenReturn(true);
    when(params.getIdFieldName()).thenReturn("id");
    when(params.getTextFieldName()).thenReturn("text");
    when(params.getMetadataFieldName()).thenReturn("metadata");
    when(params.getVectorFieldName()).thenReturn("vector");

    connection = new MilvusStoreConnection(params);
  }

  @Test
  void constructor_setsAllFieldsFromParameters() {
    assertThat(connection.getIndexType()).isEqualTo("FLAT");
    assertThat(connection.getMetricType()).isEqualTo("COSINE");
    assertThat(connection.getConsistencyLevel()).isEqualTo("EVENTUALLY");
    assertThat(connection.isAutoFlushOnInsert()).isTrue();
    assertThat(connection.getIdFieldName()).isEqualTo("id");
    assertThat(connection.getTextFieldName()).isEqualTo("text");
    assertThat(connection.getMetadataFieldName()).isEqualTo("metadata");
    assertThat(connection.getVectorFieldName()).isEqualTo("vector");
  }

  @Test
  void getVectorStore_returnsMilvusConstant() {
    assertThat(connection.getVectorStore()).isEqualTo(Constants.VECTOR_STORE_MILVUS);
  }

  @Test
  void getConnectionParameters_returnsParams() {
    assertThat(connection.getConnectionParameters()).isSameAs(params);
  }

  @Test
  void getClient_returnsNullBeforeInitialise() {
    assertThat(connection.getClient()).isNull();
  }

  @Test
  void disconnect_withNullClient_doesNotThrow() {
    assertThatCode(() -> connection.disconnect()).doesNotThrowAnyException();
  }

  @Test
  void disconnect_withClient_closesClient() throws Exception {
    MilvusServiceClient mockClient = mock(MilvusServiceClient.class);
    setClient(mockClient);
    connection.disconnect();
    verify(mockClient).close();
  }

  @Test
  void validate_callsValidationAndHealthCheck() throws Exception {
    MilvusServiceClient mockClient = mock(MilvusServiceClient.class);
    setClient(mockClient);

    @SuppressWarnings("unchecked")
    R<CheckHealthResponse> healthResponse = mock(R.class);
    when(healthResponse.getStatus()).thenReturn(0);
    when(mockClient.checkHealth()).thenReturn(healthResponse);

    try (MockedStatic<ConnectionValidationStrategies> strategies =
        mockStatic(ConnectionValidationStrategies.class)) {
      strategies.when(() -> ConnectionValidationStrategies.validateMilvus(params)).then(inv -> null);
      assertThatCode(() -> connection.validate()).doesNotThrowAnyException();
    }
  }

  @Test
  void validate_throwsModuleExceptionOnHealthCheckFailure() throws Exception {
    MilvusServiceClient mockClient = mock(MilvusServiceClient.class);
    setClient(mockClient);
    when(mockClient.checkHealth()).thenThrow(new RuntimeException("connection refused"));

    try (MockedStatic<ConnectionValidationStrategies> strategies =
        mockStatic(ConnectionValidationStrategies.class)) {
      strategies.when(() -> ConnectionValidationStrategies.validateMilvus(params)).then(inv -> null);
      assertThatThrownBy(() -> connection.validate())
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to connect to Milvus store");
    }
  }

  @Test
  void isValid_returnsTrueWhenHealthy() throws Exception {
    MilvusServiceClient mockClient = mock(MilvusServiceClient.class);
    setClient(mockClient);

    @SuppressWarnings("unchecked")
    R<CheckHealthResponse> healthResponse = mock(R.class);
    when(healthResponse.getStatus()).thenReturn(0);
    when(mockClient.checkHealth()).thenReturn(healthResponse);

    assertThat(connection.isValid()).isTrue();
  }

  @Test
  void isValid_returnsFalseWhenUnhealthy() throws Exception {
    MilvusServiceClient mockClient = mock(MilvusServiceClient.class);
    setClient(mockClient);

    @SuppressWarnings("unchecked")
    R<CheckHealthResponse> healthResponse = mock(R.class);
    when(healthResponse.getStatus()).thenReturn(1);
    when(mockClient.checkHealth()).thenReturn(healthResponse);

    assertThat(connection.isValid()).isFalse();
  }

  private void setClient(MilvusServiceClient client) throws Exception {
    java.lang.reflect.Field clientField = MilvusStoreConnection.class.getDeclaredField("client");
    clientField.setAccessible(true);
    clientField.set(connection, client);
  }
}
