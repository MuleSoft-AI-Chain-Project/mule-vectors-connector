package org.mule.extension.vectors.internal.model.text.vertexai;

import java.util.HashMap;
import java.util.Map;

// See: https://cloud.google.com/vertex-ai/generative-ai/docs/learn/model-versions#embeddings_stable_model_versions
// This enum is used to define the embedding model names and their dimensions for Vertex AI.
public enum VertexAiEmbeddingModelName {
  TEXT_EMBEDDING_004("text-embedding-005", 768),
  TEXT_MULTILINGUAL_EMBEDDING_002("text-multilingual-embedding-002", 768),

  private final String stringValue;
  private final Integer dimension;
  private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap<>(values().length);

  private VertexAiEmbeddingModelName(String stringValue, Integer dimension) {
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
    VertexAiEmbeddingModelName[] var0 = values();
    int var1 = var0.length;

    for(int i = 0; i < var1; ++i) {
      VertexAiEmbeddingModelName embeddingModelName = var0[i];
      KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
    }

  }
}
