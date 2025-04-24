package org.mule.extension.vectors.internal.model.multimodal.nomic;

import java.util.HashMap;
import java.util.Map;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;

public enum NomicEmbeddingMultimodalModelName {
  NOMIC_EMBED_VISION_V1(EmbeddingModelHelper.MultimodalEmbeddingModelNames.NOMIC_EMBED_VISION_V1.getModelName(), 768),
  NOMIC_EMBED_VISION_V1_5(EmbeddingModelHelper.MultimodalEmbeddingModelNames.NOMIC_EMBED_VISION_V1_5.getModelName(), 768);

  private final String stringValue;
  private final Integer dimension;
  private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap(values().length);

  private NomicEmbeddingMultimodalModelName(String stringValue, Integer dimension) {
    this.stringValue = stringValue;
    this.dimension = dimension;
  }

  public String toString() {
    return this.stringValue;
  }

  public Integer dimension() {
    return this.dimension;
  }

  public static Integer knownDimension(String modelName) {
    return (Integer)KNOWN_DIMENSION.get(modelName);
  }

  static {
    NomicEmbeddingMultimodalModelName[] var0 = values();
    int var1 = var0.length;

    for(int i = 0; i < var1; ++i) {
      NomicEmbeddingMultimodalModelName embeddingModelName = var0[i];
      KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
    }

  }
}
