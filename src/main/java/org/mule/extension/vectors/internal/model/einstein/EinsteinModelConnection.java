package org.mule.extension.vectors.internal.model.einstein;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Alias("einstein")
@DisplayName("Einstein")
public class EinsteinModelConnection extends BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinModelConnection.class);

  public static final String URI_OAUTH_TOKEN = "/services/oauth2/token";
  public final String QUERY_PARAM_GRANT_TYPE = "grant_type";
  public final String QUERY_PARAM_CLIENT_ID = "client_id";
  public final String QUERY_PARAM_CLIENT_SECRET = "client_secret";
  public final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  @Parameter
  @Alias("salesforceOrg")
  @DisplayName("Salesforce Org")
  @Summary("The salesforce org.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1, tab = "Embedding Model Connection")
  @Example("mydomain.my.salesforce.com")
  private String salesforceOrg;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2, tab = "Embedding Model Connection")
  @Example("<your-connected-app-client-id>")
  private String clientId;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3, tab = "Embedding Model Connection")
  @Example("<your-connected-app-client-secret>")
  private String clientSecret;

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN;
  }

  public EinsteinModelConnection() {

  }

  public EinsteinModelConnection(String salesforceOrg, String clientId, String clientSecret) {

    this.salesforceOrg = salesforceOrg;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  public String getSalesforceOrg() {
    return salesforceOrg;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  @Override
  public EinsteinModelConnection connect()  throws ConnectionException {

    LOGGER.debug(String.format("Connect to Salesforce. salesforceOrg: %s, clientId: %s, clientSecret: %s",
       salesforceOrg, clientId, clientSecret));

    try {

      int responseCode = getConnectionResponseCode(salesforceOrg, clientId, clientSecret);
      if (responseCode == 200) {
        return new EinsteinModelConnection(salesforceOrg, clientId, clientSecret);
      } else {
        throw new ConnectionException("Failed to connect to Salesforce: HTTP " + responseCode);
      }
    } catch (IOException e) {
      throw new ConnectionException("Failed to connect to Salesforce", e);
    }
  }

  @Override
  public void disconnect(BaseModelConnection connection) {
    try {
      // Add logic to invalidate the connection if necessary
    } catch (Exception e) {

      LOGGER.error("Error while disconnecting [{}]: {}", getClientId(), e.getMessage(), e);
    }
  }

  @Override
  public ConnectionValidationResult validate(BaseModelConnection connection) {

    EinsteinModelConnection einsteinModelConnection = (EinsteinModelConnection) connection;
    try {
      int responseCode =
          getConnectionResponseCode(einsteinModelConnection.getSalesforceOrg(), einsteinModelConnection.getClientId(), einsteinModelConnection.getClientSecret());

      if (responseCode == 200) {
        return ConnectionValidationResult.success();
      } else {
        return ConnectionValidationResult.failure("Failed to validate connection: HTTP " + responseCode, null);
      }
    } catch (IOException e) {
      return ConnectionValidationResult.failure("Failed to validate connection", e);
    }
  }

  private int getConnectionResponseCode(String salesforceOrg, String clientId, String clientSecret) throws IOException {

    LOGGER.debug("Preparing request for connection for salesforce org:{}", salesforceOrg);

    String urlStr = getOAuthURL();
    String urlParameters = getOAuthParams();

    byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

    URL url = new URL(urlStr);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod(Constants.HTTP_METHOD_POST);
    conn.getOutputStream().write(postData);
    int respCode = conn.getResponseCode();

    LOGGER.debug("Response code for connection request:{}", respCode);
    return respCode;
  }

  public String getOAuthURL() {
    return Constants.URI_HTTPS_PREFIX + salesforceOrg + URI_OAUTH_TOKEN;
  }

  public String getOAuthParams() {
    return QUERY_PARAM_GRANT_TYPE + "=" + GRANT_TYPE_CLIENT_CREDENTIALS
        + "&" + QUERY_PARAM_CLIENT_ID + "=" + clientId
        + "&" + QUERY_PARAM_CLIENT_SECRET + "=" + clientSecret;
  }
}
