package org.mule.extension.vectors.internal.connection.model.vertexai;

import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.aiplatform.v1beta1.LlmUtilityServiceClient;
import com.google.cloud.aiplatform.v1beta1.LlmUtilityServiceSettings;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1beta1.PredictionServiceSettings;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.threeten.bp.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class VertexAIModelConnection implements BaseModelConnection {

  private static final String DEFAULT_GOOGLEAPIS_ENDPOINT_SUFFIX = "-aiplatform.googleapis.com:443";

  private static final String[] SCOPES = {
      "https://www.googleapis.com/auth/cloud-platform",
      "https://www.googleapis.com/auth/cloud-platform.read-only"
  };

  private String projectId;
  private String location;
  private String clientEmail;
  private String clientId;
  private String privateKeyId;
  private String privateKey;

  private int maxAttempts;
  private long initialRetryDelay;
  private double retryDelayMultiplier;
  private long maxRetryDelay;
  private long totalTimeout;

  private PredictionServiceClient predictionClient;
  private LlmUtilityServiceClient llmUtilityServiceClient;

  public LlmUtilityServiceClient getLlmUtilityServiceClient() {

    if(this.llmUtilityServiceClient == null) {

      try {

        LlmUtilityServiceSettings llmUtilityServiceSettings = LlmUtilityServiceSettings.newBuilder()
            .setEndpoint(location + DEFAULT_GOOGLEAPIS_ENDPOINT_SUFFIX)
            .setCredentialsProvider(() -> getCredentials())
            .build();

        this.llmUtilityServiceClient = LlmUtilityServiceClient.create(llmUtilityServiceSettings);

      } catch (IOException e) {

        throw new ModuleException(
            "Failed to initiate LlmUtilityService client for VERTEX AI service.",
            MuleVectorsErrorType.AI_SERVICES_FAILURE,
            e);
      }
    }
    return this.llmUtilityServiceClient;
  }

  public VertexAIModelConnection(String projectId, String location, String clientEmail, String clientId, String privateKeyId,
                                 String privateKey, int maxAttempts, long initialRetryDelay, double retryDelayMultiplier,
                                 long maxRetryDelay, long totalTimeout) {
    this.projectId = projectId;
    this.location = location;
    this.clientEmail = clientEmail;
    this.clientId = clientId;
    this.privateKeyId = privateKeyId;
    this.privateKey = privateKey;
    this.maxAttempts = maxAttempts;
    this.initialRetryDelay = initialRetryDelay;
    this.retryDelayMultiplier = retryDelayMultiplier;
    this.maxRetryDelay = maxRetryDelay;
    this.totalTimeout = totalTimeout;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getLocation() {
    return location;
  }

  public String getClientEmail() {
    return clientEmail;
  }

  public String getClientId() {
    return clientId;
  }

  public String getPrivateKeyId() {
    return privateKeyId;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public int getMaxAttempts() { return maxAttempts; }

  public long getInitialRetryDelay() { return initialRetryDelay; }

  public double getRetryDelayMultiplier() { return retryDelayMultiplier; }

  public long getMaxRetryDelay() { return maxRetryDelay; }

  public long getTotalTimeout() { return totalTimeout; }

  public PredictionServiceClient getPredictionClient() {
    return predictionClient;
  }

  @Override
  public void connect() throws ConnectionException {

    try {

      // Configure custom retry settings
      RetrySettings retrySettings = RetrySettings.newBuilder()
          .setMaxAttempts(maxAttempts > 0 ? maxAttempts : 3) // Maximum number of retries
          .setInitialRetryDelay(Duration.ofMillis(initialRetryDelay > 0 ? initialRetryDelay : 500)) // Initial retry delay
          .setRetryDelayMultiplier(retryDelayMultiplier > 0 ? retryDelayMultiplier : 1.5) // Multiplier for subsequent retries
          .setMaxRetryDelay(Duration.ofMillis(maxRetryDelay > 0 ? maxRetryDelay : 5000)) // Maximum retry delay
          .setTotalTimeout(Duration.ofMillis(totalTimeout > 0 ? totalTimeout : 60000)) // Total timeout for the operation
          .build();

      // Customize the predict settings
      PredictionServiceSettings.Builder settingsBuilder = PredictionServiceSettings.newBuilder()
          .setEndpoint(location + DEFAULT_GOOGLEAPIS_ENDPOINT_SUFFIX)
          .setCredentialsProvider(() -> getCredentials());

      // Apply retry settings to the predictSettings
      settingsBuilder
          .predictSettings()
          .setRetrySettings(retrySettings);

      // Build the PredictionServiceClient
      this.predictionClient = PredictionServiceClient.create(settingsBuilder.build());

    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Vertex AI.", e);
    }
  }

  @Override
  public void disconnect() {
    if (this.predictionClient != null) {
      this.predictionClient.close();
    }
  }

  @Override
  public boolean isValid() {
    try {

      return !predictionClient.isShutdown();
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_VERTEX_AI;
  }

  private Credentials getCredentials() throws IOException {

    ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(
        new ByteArrayInputStream(buildJsonCredentials().getBytes())
    ).toBuilder().setScopes(Arrays.asList(SCOPES)).build();
    return credentials;
  }

  private String buildJsonCredentials() {
    return new StringBuilder()
        .append("{")
        .append("\"type\": \"service_account\",")
        .append("\"project_id\": \"").append(this.projectId).append("\",")
        .append("\"private_key_id\": \"").append(this.privateKeyId).append("\",")
        .append("\"private_key\": \"").append(this.privateKey).append("\",")
        .append("\"client_email\": \"").append(this.clientEmail).append("\",")
        .append("\"client_id\": \"").append(this.clientId).append("\",")
        .append("\"auth_uri\": \"").append(Constants.GCP_AUTH_URI).append("\",")
        .append("\"token_uri\": \"").append(Constants.GCP_TOKEN_URI).append("\",")
        .append("\"auth_provider_x509_cert_url\": \"").append(Constants.GCP_AUTH_PROVIDER_X509_CERT_URL).append("\",")
        .append("\"client_x509_cert_url\": \"").append(Constants.GCP_CLIENT_X509_CERT_URL).append(this.clientEmail).append("\",")
        .append("\"universe_domain\": \"googleapis.com\"")
        .append("}")
        .toString();
  }
}
