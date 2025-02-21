package org.mule.extension.vectors.internal.connection.model.nomic;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.*;

public class NomicModelConnectionParameters extends BaseModelConnectionParameters {

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("<your-api-key>")
  private String apiKey;

  @Parameter
  @DisplayName("Max attemps")
  @Summary("Maximum number of retries")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1, tab = Placement.ADVANCED_TAB)
  @Example("3")
  @Optional(defaultValue = "3")
  private int maxAttempts;

  @Parameter
  @DisplayName("Timeout")
  @Summary("Timeout for the operation in milliseconds")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2, tab = Placement.ADVANCED_TAB)
  @Example("60000")
  @Optional(defaultValue = "60000")
  private long totalTimeout;

  public String getApiKey() {
    return apiKey;
  }

  public int getMaxAttempts() { return maxAttempts; }

  public long getTotalTimeout() { return totalTimeout; }
}
