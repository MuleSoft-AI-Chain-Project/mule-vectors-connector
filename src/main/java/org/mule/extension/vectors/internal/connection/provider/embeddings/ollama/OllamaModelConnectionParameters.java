package org.mule.extension.vectors.internal.connection.provider.embeddings.ollama;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class OllamaModelConnectionParameters extends BaseModelConnectionParameters {

  @Parameter
  @DisplayName("Base URL")
  @Summary("Ollama base URL")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("http://127.0.0.1:11434")
  private String baseUrl;

  @Parameter
  @DisplayName("Timeout")
  @Summary("Timeout for the operation in milliseconds")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2, tab = Placement.ADVANCED_TAB)
  @Example("60000")
  @Optional(defaultValue = "60000")
  private long totalTimeout;

  public String getBaseUrl() {
    return baseUrl;
  }

  public long getTotalTimeout() { return totalTimeout; }
}
