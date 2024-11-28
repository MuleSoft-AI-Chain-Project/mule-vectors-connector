package org.mule.extension.vectors.internal.store.qdrant;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.store.BaseStoreConfiguration;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("qdrant")
@DisplayName("Qdrant")
public class QdrantStoreConfiguration implements BaseStoreConfiguration {

  private final String vectorStore = Constants.VECTOR_STORE_OPENSEARCH;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @DisplayName("GPRC Port")
  @Placement(order = 2)
  @Example("6334")
  private int gprcPort;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  @Example("false")
  private boolean useTLS;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4)
  @Example("text-segment")
  private String textSegmentKey;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 5)
  @Example("<your-apikey>")
  private String apiKey;

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_QDRANT;
  }

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
