package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class QueryParameters {

  @Parameter
  @Alias("retrieveEmbeddings")
  @Expression(ExpressionSupport.SUPPORTED)
  @Summary("Flag to indicate whether embeddings should be retrieved. Defaults to true.")
  @Placement(order = 1)
  @Optional(defaultValue = "false")
  private boolean retrieveEmbeddings;

  @Parameter
  @DisplayName("Page size")
  @Alias("pageSize")
  @Expression(ExpressionSupport.SUPPORTED)
  @Summary("The page size used when querying the vector store. Defaults to 5000 rows.")
  @Placement(order = 2)
  @Optional(defaultValue = "5000")
  private Number pageSize;

  public int pageSize() {return pageSize != null ? pageSize.intValue() : 5000;}

  public boolean retrieveEmbeddings() {
    return retrieveEmbeddings;
  }
}
