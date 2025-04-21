package org.mule.extension.vectors.internal.model.text.nomic;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class NomicModel extends BaseModel {

    private final NomicModelConnection nomicModelConnection;

    public NomicModel(EmbeddingConfiguration embeddingConfiguration, NomicModelConnection nomicModelConnection, EmbeddingModelParameters embeddingModelParameters) {
        super(embeddingConfiguration, nomicModelConnection, embeddingModelParameters);
        this.nomicModelConnection = nomicModelConnection;
    }

    @Override
    public EmbeddingModel buildEmbeddingModel() {
        return NomicEmbeddingModel.builder()
            .connection(nomicModelConnection)
            .modelName(embeddingModelParameters.getEmbeddingModelName())
            .build();
    }
}
