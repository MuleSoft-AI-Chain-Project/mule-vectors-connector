package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface AzureAIVisionAPI {

  @Headers("Content-Type: application/json")
  @POST("computervision/retrieval:vectorizeText")
  Call<AzureAIVisionEmbeddingResponseBody> embedText(
      @Body AzureAIVisionTextEmbeddingRequestBody body,
      @Header("Ocp-Apim-Subscription-Key") String apiKey,
      @Query("api-version") String apiVersion,
      @Query("model-version") String modelVersion
  );

  @Headers("Content-Type: application/octet-stream")
  @POST("computervision/retrieval:vectorizeImage")
  Call<AzureAIVisionEmbeddingResponseBody> embedImage(
      @Body RequestBody body,
      @Header("Ocp-Apim-Subscription-Key") String apiKey,
      @Query("api-version") String apiVersion,
      @Query("model-version") String modelVersion
  );

  @GET("computervision/models")
  Call<AzureAIVisionModelResponse> getModels(
      @Header("Ocp-Apim-Subscription-Key") String apiKey,
      @Query("api-version") String apiVersion
  );
}
