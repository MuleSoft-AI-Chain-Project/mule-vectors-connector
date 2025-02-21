package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

import java.util.HashMap;
import java.util.Map;

public enum AzureAIVisionEmbeddingMultimodalModelName {
  AZURE_AI_VISION_2022_04_11("2022-04-11", 1024),
  AZURE_AI_VISION_2023_04_15("2023-04-15", 1024);

  private final String stringValue;
  private final Integer dimension;
  private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap(values().length);

  private AzureAIVisionEmbeddingMultimodalModelName(String stringValue, Integer dimension) {
    this.stringValue = stringValue;
    this.dimension = dimension;
  }

  public String toString() {
    return this.stringValue;
  }

  public Integer dimension() {
    return this.dimension;
  }

  public static String getDefaultModelName() {
    return AZURE_AI_VISION_2023_04_15.toString();
  }

  public static Integer knownDimension(String modelName) {
    return (Integer)KNOWN_DIMENSION.get(modelName);
  }

  static {
    AzureAIVisionEmbeddingMultimodalModelName[] var0 = values();
    int var1 = var0.length;

    for(int i = 0; i < var1; ++i) {
      AzureAIVisionEmbeddingMultimodalModelName embeddingModelName = var0[i];
      KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
    }

  }
}
