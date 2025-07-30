package org.mule.extension.vectors.internal.connection.provider.store.qdrant;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class QdrantStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Example("localhost")
  @Summary("This vector store is supported as beta. Please refer to the product documentation.")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @DisplayName("GPRC Port")
  @Example("6334")
  private int gprcPort;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Example("false")
  private boolean useTLS;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Example("text-segment")
  private String textSegmentKey;

  @Parameter
  @Password
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Example("<your-apikey>")
  private String apiKey;

  public String getHost() {
    return host;
  }

  public int getGprcPort() {
    return gprcPort;
  }

  public boolean isUseTLS() {
    return useTLS;
  }

  public String getTextSegmentKey() {
    return textSegmentKey;
  }

  public String getApiKey() {
    return apiKey;
  }
}
