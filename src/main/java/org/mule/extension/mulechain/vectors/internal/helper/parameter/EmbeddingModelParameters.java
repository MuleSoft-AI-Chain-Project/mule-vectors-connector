package org.mule.extension.mulechain.vectors.internal.helper.parameter;

import org.mule.extension.mulechain.vectors.internal.helper.provider.EmbeddingModelNameProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.values.OfValues;

public class EmbeddingModelParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(EmbeddingModelNameProvider.class)
  private String embeddingModelName;

  public String getEmbeddingModelName() {
    return embeddingModelName;
  }

}