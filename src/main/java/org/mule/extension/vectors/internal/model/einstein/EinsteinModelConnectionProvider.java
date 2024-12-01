package org.mule.extension.vectors.internal.model.einstein;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.extension.vectors.internal.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Alias("einstein")
@DisplayName("Einstein")
public class EinsteinModelConnectionProvider extends BaseModelConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinModelConnectionProvider.class);

  public static final String URI_OAUTH_TOKEN = "/services/oauth2/token";
  public static final String QUERY_PARAM_GRANT_TYPE = "grant_type";
  public static final String QUERY_PARAM_CLIENT_ID = "client_id";
  public static final String QUERY_PARAM_CLIENT_SECRET = "client_secret";
  public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private EinsteinModelConnectionParameters einsteinModelConnectionParameters;

  @Override
  public BaseModelConnection connect() throws ConnectionException {

    LOGGER.debug(String.format("Connect to Salesforce. salesforceOrg: %s, clientId: %s, clientSecret: %s",
                               einsteinModelConnectionParameters.getSalesforceOrg(),
                               einsteinModelConnectionParameters.getClientId(),
                               einsteinModelConnectionParameters.getClientSecret()));

    try {

      int responseCode = getConnectionResponseCode(einsteinModelConnectionParameters.getSalesforceOrg(),
                                                   einsteinModelConnectionParameters.getClientId(),
                                                   einsteinModelConnectionParameters.getClientSecret());
      if (responseCode == 200) {
        return new EinsteinModelConnection(einsteinModelConnectionParameters.getSalesforceOrg(),
                                           einsteinModelConnectionParameters.getClientId(),
                                           einsteinModelConnectionParameters.getClientSecret());
      } else {
        throw new ConnectionException("Failed to connect to Salesforce: HTTP " + responseCode);
      }
    } catch (IOException e) {
      throw new ConnectionException("Failed to connect to Salesforce", e);
    }
  }

  @Override
  public void disconnect(BaseModelConnection connection) {

    EinsteinModelConnection einsteinModelConnection = (EinsteinModelConnection) connection;
    try {
      // Add logic to invalidate the connection if necessary
    } catch (Exception e) {

      LOGGER.error("Error while disconnecting [{}]: {}", einsteinModelConnection.getClientId(), e.getMessage(), e);
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

    String urlStr = getOAuthURL(salesforceOrg);
    String urlParameters = getOAuthParams(clientId, clientSecret);

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

  public static String getOAuthURL(String salesforceOrg) {
    return Constants.URI_HTTPS_PREFIX + salesforceOrg + URI_OAUTH_TOKEN;
  }

  public static String getOAuthParams(String clientId, String clientSecret) {
    return QUERY_PARAM_GRANT_TYPE + "=" + GRANT_TYPE_CLIENT_CREDENTIALS
        + "&" + QUERY_PARAM_CLIENT_ID + "=" + clientId
        + "&" + QUERY_PARAM_CLIENT_SECRET + "=" + clientSecret;
  }
}
