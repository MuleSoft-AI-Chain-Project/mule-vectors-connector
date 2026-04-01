package org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VertexAIModelConnectionTest {

  private static String TEST_PEM;

  @Mock
  HttpClient httpClient;

  VertexAIModelConnection connection;

  @BeforeAll
  static void generateKey() throws Exception {
    var kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    var kp = kpg.generateKeyPair();
    String encoded = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
    TEST_PEM = "-----BEGIN PRIVATE KEY-----\n" + encoded + "\n-----END PRIVATE KEY-----";
  }

  @BeforeEach
  void setUp() {
    connection = new VertexAIModelConnection(
                                             "project-id", "us-central1", "test@test.iam.gserviceaccount.com",
                                             "client-id", "private-key-id", TEST_PEM, 5000L, 16, httpClient);
  }

  @Test
  void constructor_setsAllFields() {
    assertThat(connection.getProjectId()).isEqualTo("project-id");
    assertThat(connection.getLocation()).isEqualTo("us-central1");
    assertThat(connection.getClientId()).isEqualTo("client-id");
    assertThat(connection.getPrivateKeyId()).isEqualTo("private-key-id");
    assertThat(connection.getPrivateKey()).isEqualTo(TEST_PEM);
    assertThat(connection.getHttpClient()).isSameAs(httpClient);
    assertThat(connection.getTotalTimeout()).isEqualTo(5000L);
    assertThat(connection.getBatchSize()).isEqualTo(16);
    assertThat(connection.getObjectMapper()).isNotNull();
    assertThat(connection.getEmbeddingModelService()).isEqualTo("VERTEX_AI");
  }

  @Test
  void disconnect_doesNotThrow() {
    assertThatCode(() -> connection.disconnect()).doesNotThrowAnyException();
  }

  @Test
  void createJwt_returnsValidJwtFormat() {
    String jwt = connection.createJwt();
    assertThat(jwt).isNotNull();
    String[] parts = jwt.split("\\.");
    assertThat(parts).hasSize(3);
  }

  @Test
  void createJwt_throwsOnInvalidKey() {
    VertexAIModelConnection badConn = new VertexAIModelConnection(
                                                                  "p", "l", "e", "c", "k", "not-a-key", 1000L, 1, httpClient);
    assertThatThrownBy(badConn::createJwt)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Failed to create JWT");
  }

  @Test
  void getOrRefreshToken_noExistingToken_refreshes() throws Exception {
    String tokenJson = "{\"access_token\":\"new-token\"}";
    HttpResponse resp = mock(HttpResponse.class);
    when(resp.getStatusCode()).thenReturn(200);
    when(resp.getEntity()).thenReturn(new ByteArrayHttpEntity(tokenJson.getBytes(StandardCharsets.UTF_8)));

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(resp));
      String token = connection.getOrRefreshToken().get();
      assertThat(token).isEqualTo("new-token");
    }
  }

  @Test
  void getOrRefreshToken_existingValidToken_reuses() throws Exception {
    String tokenJson = "{\"access_token\":\"first-token\"}";
    HttpResponse tokenResp = mock(HttpResponse.class);
    when(tokenResp.getStatusCode()).thenReturn(200);
    when(tokenResp.getEntity()).thenReturn(new ByteArrayHttpEntity(tokenJson.getBytes(StandardCharsets.UTF_8)));

    HttpResponse validationResp = mock(HttpResponse.class);
    when(validationResp.getStatusCode()).thenReturn(200);
    when(validationResp.getEntity()).thenReturn(
                                                new ByteArrayHttpEntity("{\"expires_in\":3600}"
                                                    .getBytes(StandardCharsets.UTF_8)));

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(tokenResp));
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(validationResp));

      connection.getOrRefreshToken().get();
      String token2 = connection.getOrRefreshToken().get();
      assertThat(token2).isEqualTo("first-token");
    }
  }

  @Test
  void getOrRefreshToken_expiredToken_refreshes() throws Exception {
    String tokenJson1 = "{\"access_token\":\"old-token\"}";
    String tokenJson2 = "{\"access_token\":\"new-token\"}";

    HttpResponse tokenResp1 = mock(HttpResponse.class);
    when(tokenResp1.getStatusCode()).thenReturn(200);
    when(tokenResp1.getEntity()).thenReturn(new ByteArrayHttpEntity(tokenJson1.getBytes(StandardCharsets.UTF_8)));

    HttpResponse tokenResp2 = mock(HttpResponse.class);
    when(tokenResp2.getStatusCode()).thenReturn(200);
    when(tokenResp2.getEntity()).thenReturn(new ByteArrayHttpEntity(tokenJson2.getBytes(StandardCharsets.UTF_8)));

    HttpResponse validationResp = mock(HttpResponse.class);
    when(validationResp.getStatusCode()).thenReturn(401);

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(tokenResp1))
          .thenReturn(CompletableFuture.completedFuture(tokenResp2));
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(validationResp));

      connection.getOrRefreshToken().get();
      String token2 = connection.getOrRefreshToken().get();
      assertThat(token2).isEqualTo("new-token");
    }
  }

  @Test
  void validate_successfulToken() throws Exception {
    String tokenJson = "{\"access_token\":\"valid-token\"}";
    HttpResponse resp = mock(HttpResponse.class);
    when(resp.getStatusCode()).thenReturn(200);
    when(resp.getEntity()).thenReturn(new ByteArrayHttpEntity(tokenJson.getBytes(StandardCharsets.UTF_8)));

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(resp));
      assertThatCode(() -> connection.validate()).doesNotThrowAnyException();
    }
  }

  @Test
  void validate_failedToken_throwsModuleException() throws Exception {
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      CompletableFuture<HttpResponse> failedFuture = new CompletableFuture<>();
      failedFuture.completeExceptionally(new RuntimeException("connection refused"));
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(failedFuture);
      assertThatThrownBy(() -> connection.validate())
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to validate connection");
      Thread.interrupted();
    }
  }

  @Test
  void refreshAccessTokenAsync_non200Response_throwsModuleException() throws Exception {
    HttpResponse errorResp = mock(HttpResponse.class);
    when(errorResp.getStatusCode()).thenReturn(401);
    when(errorResp.getEntity()).thenReturn(
                                           new ByteArrayHttpEntity("unauthorized".getBytes(StandardCharsets.UTF_8)));

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(errorResp));
      assertThatThrownBy(() -> connection.getOrRefreshToken().get())
          .isInstanceOf(ExecutionException.class)
          .hasCauseInstanceOf(ModuleException.class);
    }
  }

  @Test
  void validateAccessTokenAsync_parseError_returnsFalse() throws Exception {
    HttpResponse badResp = mock(HttpResponse.class);
    when(badResp.getStatusCode()).thenReturn(200);
    when(badResp.getEntity()).thenReturn(
                                         new ByteArrayHttpEntity("not-json".getBytes(StandardCharsets.UTF_8)));

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(badResp));
      Boolean result = connection.validateAccessTokenAsync("test-token").get();
      assertThat(result).isFalse();
    }
  }

  @Test
  void validateAccessTokenAsync_exception_returnsFalse() throws Exception {
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      CompletableFuture<HttpResponse> failedFuture = new CompletableFuture<>();
      failedFuture.completeExceptionally(new RuntimeException("network error"));
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(failedFuture);
      Boolean result = connection.validateAccessTokenAsync("test-token").get();
      assertThat(result).isFalse();
    }
  }
}
