package org.mule.extension.vectors.internal.model.multimodal.nomic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class NomicClient {

  private static final ObjectMapper OBJECT_MAPPER;
  private final NomicApi nomicApi;
  private final String authorizationHeader;

  NomicClient(String baseUrl, String apiKey, Duration timeout) {

    OkHttpClient.Builder okHttpClientBuilder = (new OkHttpClient.Builder())
        .callTimeout(timeout)
        .connectTimeout(timeout)
        .readTimeout(timeout)
        .writeTimeout(timeout);
    
    Retrofit retrofit = (new Retrofit.Builder())
        .baseUrl(Utils.ensureTrailingForwardSlash(baseUrl))
        .client(okHttpClientBuilder.build())
        .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
        .build();

    this.nomicApi = (NomicApi)retrofit.create(NomicApi.class);
    this.authorizationHeader = "Bearer " + ValidationUtils.ensureNotBlank(apiKey, "apiKey");
  }

  public NomicEmbeddingResponseBody embed(NomicImageEmbeddingRequestBody request) {

    try {

      RequestBody modelBody = RequestBody.create(request.getModel(), MediaType.get("text/plain"));

      List<MultipartBody.Part> imageParts = request.getImages().stream()
          .map(file -> {
            RequestBody requestFile = RequestBody.create(file, MediaType.get("image/*"));
            return MultipartBody.Part.createFormData("images", "image", requestFile);
          })
          .collect(Collectors.toList());

      Response<NomicEmbeddingResponseBody> retrofitResponse =
          this.nomicApi.embed(modelBody, imageParts, this.authorizationHeader).execute();

      if (retrofitResponse.isSuccessful()) {
        return (NomicEmbeddingResponseBody)retrofitResponse.body();
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

  public static NomicClientBuilder builder() {
    return new NomicClientBuilder();
  }

  static {
    OBJECT_MAPPER = (new ObjectMapper()).setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).enable(
        SerializationFeature.INDENT_OUTPUT).setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static class NomicClientBuilder {
    private String baseUrl;
    private String apiKey;
    private Duration timeout;

    NomicClientBuilder() {
    }

    public NomicClientBuilder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public NomicClientBuilder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public NomicClientBuilder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    public NomicClient build() {
      return new NomicClient(this.baseUrl, this.apiKey, this.timeout);
    }

    public String toString() {
      return "NomicClient.NomicClientBuilder(baseUrl=" + this.baseUrl + ", apiKey=" + this.apiKey + ", timeout=" + this.timeout + ")";
    }
  }
}
