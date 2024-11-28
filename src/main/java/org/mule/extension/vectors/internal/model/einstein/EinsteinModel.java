package org.mule.extension.vectors.internal.model.einstein;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EinsteinModel  extends BaseModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinModel.class);

  private final EinsteinModelConnection einsteinModelConnection;
  private final String modelName;

  public EinsteinModel(EinsteinModelConnection einsteinModelConnection, EmbeddingModelParameters embeddingModelParameters) {

    super(einsteinModelConnection,embeddingModelParameters);

    this.einsteinModelConnection = einsteinModelConnection;
    this.modelName = embeddingModelParameters.getEmbeddingModelName();
  }

  public EmbeddingModel buildEmbeddingModel() {

    return EinsteinEmbeddingModel.builder()
        .modelConnection(einsteinModelConnection)
        .modelName(modelName)
        .build();
  }
}
