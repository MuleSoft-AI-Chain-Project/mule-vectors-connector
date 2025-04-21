package org.mule.extension.vectors.internal.model.text.mistralai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class MistralAIModel extends BaseModel {

    private final MistralAIModelConnection mistralAIModelConnection;

    public MistralAIModel(EmbeddingConfiguration embeddingConfiguration, 
                         MistralAIModelConnection mistralAIModelConnection,
                         EmbeddingModelParameters embeddingModelParameters) {
        super(embeddingConfiguration, mistralAIModelConnection, embeddingModelParameters);
        this.mistralAIModelConnection = mistralAIModelConnection;
    }

    public EmbeddingModel buildEmbeddingModel() {
        return MistralAIEmbeddingModel.builder()
            .connection(mistralAIModelConnection)
            .modelName(embeddingModelParameters.getEmbeddingModelName())
            .build();
    }
}
