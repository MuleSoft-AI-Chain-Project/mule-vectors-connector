package org.mule.extension.vectors.internal.model.text.azureopenai;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;

import java.util.HashMap;
import java.util.Map;

public enum AzureOpenAIEmbeddingModelName {

    TEXT_EMBEDDING_3_SMALL(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_3_SMALL.getModelName(), 1536), // alias for the latest text-embedding-3-small model
    TEXT_EMBEDDING_3_SMALL_1(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_3_SMALL_1.getModelName(), 1536),
    TEXT_EMBEDDING_3_LARGE(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_3_LARGE.getModelName(), 3072),
    TEXT_EMBEDDING_3_LARGE_1(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_3_LARGE_1.getModelName(), 3072),

    TEXT_EMBEDDING_ADA_002(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_ADA_002.getModelName(), 1536), // alias for the latest text-embedding-ada-002 model
    TEXT_EMBEDDING_ADA_002_1(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_ADA_002_1.getModelName(), 1536),
    TEXT_EMBEDDING_ADA_002_2(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_ADA_002_2.getModelName(), 1536);

    private final String stringValue;
    private final Integer dimension;
    private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap<String, Integer>(values().length);

  private AzureOpenAIEmbeddingModelName(String stringValue, Integer dimension) {
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
    AzureOpenAIEmbeddingModelName[] var0 = values();
    int var1 = var0.length;

    for(int var2 = 0; var2 < var1; ++var2) {
      AzureOpenAIEmbeddingModelName embeddingModelName = var0[var2];
      KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
    }
  }
}
