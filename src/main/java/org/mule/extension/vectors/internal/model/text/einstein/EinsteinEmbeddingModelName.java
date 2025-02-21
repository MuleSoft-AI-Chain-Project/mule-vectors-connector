package org.mule.extension.vectors.internal.model.text.einstein;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;

import java.util.HashMap;
import java.util.Map;

public enum EinsteinEmbeddingModelName {

  DEFAULT_ADA_002(EmbeddingModelHelper.TextEmbeddingModelNames.SFDC_TEXT_EMBEDDING_ADA_002.getModelName(), 1536),
  AZURE_OPENAI_ADA_002(EmbeddingModelHelper.TextEmbeddingModelNames.SFDC_AZURE_TEXT_EMBEDDING_ADA_002.getModelName(), 1536),
  OPENAI_ADA_002(EmbeddingModelHelper.TextEmbeddingModelNames.SFDC_OPENAI_TEXT_EMBEDDING_ADA_002.getModelName(), 1536);

  private final String stringValue;
  private final Integer dimension;
  private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap(values().length);

  private EinsteinEmbeddingModelName(String stringValue, Integer dimension) {
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
    EinsteinEmbeddingModelName[] var0 = values();
    int var1 = var0.length;

    for(int var2 = 0; var2 < var1; ++var2) {
      EinsteinEmbeddingModelName embeddingModelName = var0[var2];
      KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
    }
  }
}
