package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

public class AzureAIVisionEmbeddingResponseBody {

  private float[] vector;
  private String modelVersion;

  AzureAIVisionEmbeddingResponseBody() {
  }

  public float[] getVector() {
    return this.vector;
  }

  public String getModelVersion() {
    return this.modelVersion;
  }

  @Override
  public String toString() {
    return "AzureAIVisionEmbeddingResponseBody{" +
        "vector=" + vector +
        ", modelVersion='" + modelVersion + '\'' +
        '}';
  }
}
