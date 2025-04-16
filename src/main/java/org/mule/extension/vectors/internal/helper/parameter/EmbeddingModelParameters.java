package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;
import org.mule.extension.vectors.internal.helper.provider.EmbeddingModelNameProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;

public class EmbeddingModelParameters {

  @Parameter
  @Alias("embeddingModelName")
  @DisplayName("Model/Deployment Name")
  @Summary("The embedding model/deployment name.")
  @Placement(order = 1)
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(EmbeddingModelNameProvider.class)
  private String embeddingModelName;

  public String getEmbeddingModelName() {
    return embeddingModelName;
  }

  public EmbeddingModelHelper.EmbeddingModelType getEmbeddingModelType() { return EmbeddingModelHelper.getModelType(embeddingModelName); }
}
