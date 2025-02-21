package org.mule.extension.vectors.internal.model.multimodal.vertexai;

import java.util.HashMap;
import java.util.Map;

public enum VertexAiEmbeddingMultimodalModelName {

  MULTIMODALEMBEDDING("multimodalembedding", 1408);

  private final String stringValue;
  private final Integer dimension;
  private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap(values().length);

  private VertexAiEmbeddingMultimodalModelName(String stringValue, Integer dimension) {
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
    VertexAiEmbeddingMultimodalModelName[] var0 = values();
    int var1 = var0.length;

    for(int i = 0; i < var1; ++i) {
      VertexAiEmbeddingMultimodalModelName embeddingModelName = var0[i];
      KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
    }

  }
}
