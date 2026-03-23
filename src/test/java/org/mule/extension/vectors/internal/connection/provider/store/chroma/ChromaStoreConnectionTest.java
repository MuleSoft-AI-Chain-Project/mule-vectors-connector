package org.mule.extension.vectors.internal.connection.provider.store.chroma;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChromaStoreConnectionTest {

  @Mock
  ChromaStoreConnectionParameters parameters;
  @Mock
  HttpClient httpClient;
  @Mock
  HttpResponse httpResponse;

  @Test
  void constructor_assignsFields() {
    when(parameters.getUrl()).thenReturn("http://chroma:8000");
    ChromaStoreConnection conn = new ChromaStoreConnection(parameters, httpClient);
    assertThat(conn.getUrl()).isEqualTo("http://chroma:8000");
    assertThat(conn.getHttpClient()).isSameAs(httpClient);
    assertThat(conn.getVectorStore()).isEqualTo(Constants.VECTOR_STORE_CHROMA);
    assertThat(conn.getConnectionParameters()).isSameAs(parameters);
  }

  @Test
  void disconnect_isNoOp() {
    when(parameters.getUrl()).thenReturn("http://chroma:8000");
    ChromaStoreConnection conn = new ChromaStoreConnection(parameters, httpClient);
    conn.disconnect();
    assertThat(conn.getHttpClient()).isSameAs(httpClient);
  }

  @Test
  void validate_successfulConnection() throws Exception {
    when(parameters.getUrl()).thenReturn("http://chroma:8000");
    ChromaStoreConnection conn = new ChromaStoreConnection(parameters, httpClient);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      when(httpResponse.getStatusCode()).thenReturn(200);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      conn.validate();
    }
  }

  @Test
  void validate_non200Response_throwsModuleException() throws Exception {
    when(parameters.getUrl()).thenReturn("http://chroma:8000");
    ChromaStoreConnection conn = new ChromaStoreConnection(parameters, httpClient);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getBytes()).thenReturn("Connection refused".getBytes());
      when(httpResponse.getStatusCode()).thenReturn(500);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      assertThatThrownBy(conn::validate)
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to connect to Chroma");
    }
  }

  @Test
  void validate_ioExceptionReadingErrorBody_throwsModuleException() throws Exception {
    when(parameters.getUrl()).thenReturn("http://chroma:8000");
    ChromaStoreConnection conn = new ChromaStoreConnection(parameters, httpClient);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getBytes()).thenThrow(new IOException("read failure"));
      when(httpResponse.getStatusCode()).thenReturn(500);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      assertThatThrownBy(conn::validate)
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to connect to Chroma");
    }
  }

  @Test
  void validate_interruptedException_throwsModuleException() throws Exception {
    when(parameters.getUrl()).thenReturn("http://chroma:8000");
    ChromaStoreConnection conn = new ChromaStoreConnection(parameters, httpClient);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      CompletableFuture<HttpResponse> future = new CompletableFuture<>();
      future.completeExceptionally(new InterruptedException("interrupted"));
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(future);

      assertThatThrownBy(conn::validate)
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to connect to Chroma");
    }
  }
}
