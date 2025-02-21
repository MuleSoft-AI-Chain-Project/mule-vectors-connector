package org.mule.extension.vectors.internal.model.multimodal.nomic;
//

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import java.util.List;

interface NomicApi {

  @Multipart
  @POST("embedding/image")
  Call<NomicEmbeddingResponseBody> embed(
      @Part("model") RequestBody model,
      @Part List<MultipartBody.Part> images,
      @Header("Authorization") String authorization
  );
}
