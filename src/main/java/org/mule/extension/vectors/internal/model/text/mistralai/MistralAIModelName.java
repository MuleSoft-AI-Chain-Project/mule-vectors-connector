package org.mule.extension.vectors.internal.model.text.mistralai;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import java.util.HashMap;
import java.util.Map;

public enum MistralAIModelName {
    @DisplayName("mistral-embed")
    MISTRAL_EMBED(EmbeddingModelHelper.TextEmbeddingModelNames.MISTRAL_EMBED.getModelName(), 1024);

    private final String stringValue;
    private final Integer dimension;
    private static final Map<String, Integer> KNOWN_DIMENSION = new HashMap<>(values().length);

    MistralAIModelName(String stringValue, Integer dimension) {
        this.stringValue = stringValue;
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }

    public Integer dimension() {
        return this.dimension;
    }

    public static Integer knownDimension(String modelName) {
        return KNOWN_DIMENSION.get(modelName);
    }

    static {
        for (MistralAIModelName embeddingModelName : values()) {
            KNOWN_DIMENSION.put(embeddingModelName.toString(), embeddingModelName.dimension());
        }
    }
}