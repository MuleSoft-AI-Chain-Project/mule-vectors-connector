package org.mule.extension.vectors.internal.connection.store.qdrant;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class QdrantStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @DisplayName("GPRC Port")
  @Placement(tab = Placement.DEFAULT_TAB, order = 2)
  @Example("6334")
  private int gprcPort;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 3)
  @Example("false")
  private boolean useTLS;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 4)
  @Example("text-segment")
  private String textSegmentKey;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 5)
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
