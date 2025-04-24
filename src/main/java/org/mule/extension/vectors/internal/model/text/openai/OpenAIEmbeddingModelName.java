package org.mule.extension.vectors.internal.model.text.openai;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;

import java.util.HashMap;
import java.util.Map;

public enum OpenAIEmbeddingModelName {

    TEXT_EMBEDDING_3_SMALL(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_3_SMALL.getModelName(), 1536),
    TEXT_EMBEDDING_3_LARGE(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_3_LARGE.getModelName(), 3072),
    TEXT_EMBEDDING_ADA_002(EmbeddingModelHelper.TextEmbeddingModelNames.TEXT_EMBEDDING_ADA_002.getModelName(), 1536);

    private final String stringValue;
    private final Integer dimension;
    private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap<String, Integer>(values().length);

    private OpenAIEmbeddingModelName(String stringValue, Integer dimension) {
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
        OpenAIEmbeddingModelName[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            OpenAIEmbeddingModelName embeddingModelName = var0[var2];
            KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
        }
    }
}