package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;

public class AzureAIVisionClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureAIVisionClient.class);

  private static final ObjectMapper OBJECT_MAPPER;
  private final OkHttpClient okHttpClient;
  private final AzureAIVisionAPI azureAIVisionAPI;
  private final String apiVersion;
  private final String apiKey;

  AzureAIVisionClient(String baseUrl, String apiVersion, String apiKey, Duration timeout) {

    OkHttpClient.Builder okHttpClientBuilder = (new OkHttpClient.Builder())
        .callTimeout(timeout)
        .connectTimeout(timeout)
        .readTimeout(timeout)
        .writeTimeout(timeout);

    this.okHttpClient = okHttpClientBuilder.build();

    Retrofit retrofit = (new Retrofit.Builder())
        .baseUrl(Utils.ensureTrailingForwardSlash(baseUrl))
        .client(okHttpClientBuilder.build())
        .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
        .build();

    this.azureAIVisionAPI = (AzureAIVisionAPI)retrofit.create(AzureAIVisionAPI.class);
    this.apiVersion = ValidationUtils.ensureNotBlank(apiVersion, "apiVersion");
    this.apiKey = ValidationUtils.ensureNotBlank(apiKey, "apiKey");
  }

  public AzureAIVisionEmbeddingResponseBody embedText(AzureAIVisionTextEmbeddingRequestBody request, String modelVersion) {

    LOGGER.debug("Embedding {} with Azure AI Vision model version: {}", request, modelVersion);

    ValidationUtils.ensureNotBlank(modelVersion, "modelVersion");

    try {

      Response<AzureAIVisionEmbeddingResponseBody> retrofitResponse =
          this.azureAIVisionAPI.embedText(request, this.apiKey, this.apiVersion, modelVersion).execute();

      LOGGER.debug("Azure AI Vision response: {}", (AzureAIVisionEmbeddingResponseBody)retrofitResponse.body());
      if (retrofitResponse.isSuccessful()) {
        return (AzureAIVisionEmbeddingResponseBody)retrofitResponse.body();
      } else {
        throw toException(retrofitResponse);
      }
    } catch (IOException var3) {

      IOException e = var3;
      throw new RuntimeException(e);
    }
  }

  public AzureAIVisionEmbeddingResponseBody embedImage(byte[] imageBytes, String modelVersion) {

    ValidationUtils.ensureNotBlank(modelVersion, "modelVersion");

    try {

      Response<AzureAIVisionEmbeddingResponseBody> retrofitResponse =
          this.azureAIVisionAPI.embedImage(RequestBody.create(imageBytes), this.apiKey, this.apiVersion, modelVersion).execute();

      if (retrofitResponse.isSuccessful()) {
        return (AzureAIVisionEmbeddingResponseBody)retrofitResponse.body();
      } else {
        throw toException(retrofitResponse);
      }
    } catch (IOException var3) {

      IOException e = var3;
      throw new RuntimeException(e);
    }
  }

  public AzureAIVisionModelResponse getModels() {

    try {

      Response<AzureAIVisionModelResponse> retrofitResponse =
          this.azureAIVisionAPI.getModels(this.apiKey, this.apiVersion).execute();

      if (retrofitResponse.isSuccessful()) {
        return (AzureAIVisionModelResponse)retrofitResponse.body();
      } else {
        throw toException(retrofitResponse);
      }
    } catch (IOException var3) {

      IOException e = var3;
      throw new RuntimeException(e);
    }
  }

  private static RuntimeException toException(Response<?> response) throws IOException {
    int code = response.code();
    String body = response.errorBody().string();
    String errorMessage = String.format("status code: %s; body: %s", code, body);
    return new RuntimeException(errorMessage);
  }

  public void close() {
    this.okHttpClient.dispatcher().executorService().shutdown();
    this.okHttpClient.connectionPool().evictAll();
  }

  public static AzureAIVisionClientBuilder builder() {
    return new AzureAIVisionClientBuilder();
  }

  static {
    OBJECT_MAPPER = (new ObjectMapper()).setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).enable(
        SerializationFeature.INDENT_OUTPUT).setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static class AzureAIVisionClientBuilder {

    private String endpoint;
    private String apiVersion;
    private String apiKey;
    private Duration timeout;

    AzureAIVisionClientBuilder() {
    }

    public AzureAIVisionClientBuilder endpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public AzureAIVisionClientBuilder apiVersion(String apiVersion) {
      this.apiVersion = apiVersion;
      return this;
    }

    public AzureAIVisionClientBuilder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public AzureAIVisionClientBuilder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    public AzureAIVisionClient build() {
      return new AzureAIVisionClient(this.endpoint, this.apiVersion, this.apiKey, this.timeout);
    }

    public String toString() {
      return "AzureAIVisionClient.AzureAIVisionClientBuilder(endpoint=" + this.endpoint +
          ", apiVersion=" + this.apiVersion +
          ", apiKey=" + this.apiKey + ", timeout=" + this.timeout + ")";
    }
  }
}
