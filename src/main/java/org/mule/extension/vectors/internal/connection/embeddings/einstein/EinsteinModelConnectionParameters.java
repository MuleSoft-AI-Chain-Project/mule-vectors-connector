package org.mule.extension.vectors.internal.connection.embeddings.einstein;

import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.*;

public class EinsteinModelConnectionParameters extends BaseModelConnectionParameters {

  @Parameter
  @Alias("salesforceOrg")
  @DisplayName("Salesforce Org")
  @Summary("The salesforce org.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Example("mydomain.my.salesforce.com")
  private String salesforceOrg;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Example("<your-connected-app-client-id>")
  private String clientId;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Example("<your-connected-app-client-secret>")
  private String clientSecret;

  public String getSalesforceOrg() {
    return salesforceOrg;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }
}
