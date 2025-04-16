package org.mule.extension.vectors.internal.connection.model.einstein;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class EinsteinModelConnection implements BaseTextModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(EinsteinModelConnection.class);

  private static final String URI_OAUTH_TOKEN = "/services/oauth2/token";
  private static final String PARAM_GRANT_TYPE = "grant_type";
  private static final String PARAM_CLIENT_ID = "client_id";
  private static final String PARAM_CLIENT_SECRET = "client_secret";
  private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
  private static final String EINSTEIN_PLATFORM_MODELS_URL = "https://api.salesforce.com/einstein/platform/v1/models/";

  private final String salesforceOrg;
  private final String clientId;
  private final String clientSecret;
  private final HttpClient httpClient;

  private String accessToken;

  public EinsteinModelConnection(String salesforceOrg, String clientId, String clientSecret, HttpClient httpClient) {
    this.salesforceOrg = salesforceOrg;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.httpClient = httpClient;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN;
  }

  @Override
  public void connect() throws ConnectionException {
    try {
      this.accessToken = getAccessToken(salesforceOrg, clientId, clientSecret);
      if (this.accessToken == null) {
        throw new ConnectionException("Failed to connect to Salesforce: HTTP " + accessToken);
      }
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Salesforce", e);
    }
  }

  @Override
  public void disconnect() {
    // Add logic to invalidate connection
  }

  @Override
  public boolean isValid() {
    return isAccessTokenValid();
  }

  private String getOAuthURL() {
    return Constants.URI_HTTPS_PREFIX + salesforceOrg + URI_OAUTH_TOKEN;
  }

  private String getOAuthParams() {
    return PARAM_GRANT_TYPE + "=" + GRANT_TYPE_CLIENT_CREDENTIALS
        + "&" + PARAM_CLIENT_ID + "=" + clientId
        + "&" + PARAM_CLIENT_SECRET + "=" + clientSecret;
  }

  private String getAccessToken(String salesforceOrg, String clientId, String clientSecret) throws ConnectionException {
    String tokenUrl = getOAuthURL();
    String oAuthParams = getOAuthParams();

    try {
      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("POST")
          .uri(tokenUrl)
          .addHeader("Content-Type", "application/x-www-form-urlencoded");

      requestBuilder.entity(new ByteArrayHttpEntity(oAuthParams.getBytes()));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout(30000)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);
      validateResponse(response);

      String responseStr = new String(response.getEntity().getBytes());
      return new JSONObject(responseStr).getString("access_token");

    } catch (Exception e) {
      throw new ConnectionException("Error while getting access token for \"EINSTEIN\" embedding model service.", e);
    }
  }

  private Boolean isAccessTokenValid() {
    String urlString = Constants.URI_HTTPS_PREFIX + salesforceOrg + "/services/oauth2/userinfo";

    try {
      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("GET")
          .uri(urlString)
          .addHeader("Authorization", "Bearer " + this.accessToken);

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout(30000)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);
      return response.getStatusCode() == 200;

    } catch (Exception e) {
      LOGGER.error("Error while validating access token for \"EINSTEIN\" embedding model service.", e);
      return false;
    }
  }

  private void validateResponse(HttpResponse response) throws IOException {
    if (response.getStatusCode() != 200) {
      String errorBody = new String(response.getEntity().getBytes());
      throw new IOException(String.format("API error (HTTP %d): %s",
          response.getStatusCode(), errorBody));
    }
  }

  private String buildEmbeddingsPayload(List<String> texts) {
    JSONArray input = new JSONArray(texts);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("input", input);
    return jsonObject.toString();
  }

  public Object generateEmbeddings(List<String> inputs, String modelName) {
    return generateEmbeddings(inputs, modelName, false);
  }

  private Object generateEmbeddings(List<String> inputs, String modelName, Boolean tokenExpired) {
    String payload = buildEmbeddingsPayload(inputs);

    try {
      String urlString = EINSTEIN_PLATFORM_MODELS_URL + modelName + "/embeddings";

      if (accessToken == null) connect();

      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("POST")
          .uri(urlString)
          .addHeader("Authorization", "Bearer " + accessToken)
          .addHeader("x-sfdc-app-context", "EinsteinGPT")
          .addHeader("x-client-feature-id", "ai-platform-models-connected-app")
          .addHeader("Content-Type", "application/json;charset=utf-8")
          .entity(new ByteArrayHttpEntity(payload.getBytes()));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout(30000)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);

      if (response.getStatusCode() == 200) {
        return new String(response.getEntity().getBytes());
      } else if (response.getStatusCode() == 401 && !tokenExpired) {
        LOGGER.debug("Salesforce access token expired.");
        connect();
        return generateEmbeddings(inputs, modelName, true);
      } else {
        String responseBody = new String(response.getEntity().getBytes());
        LOGGER.error("Error (HTTP {}): {}", response.getStatusCode(), responseBody);

        MuleVectorsErrorType muleVectorsErrorType = response.getStatusCode() == 429 ?
            MuleVectorsErrorType.AI_SERVICES_RATE_LIMITING_ERROR : MuleVectorsErrorType.AI_SERVICES_FAILURE;

        throw new ModuleException(
            String.format("Error while generating embeddings with \"EINSTEIN\" embedding model service. Response code: %s. Response %s.",
                response.getStatusCode(), responseBody),
            muleVectorsErrorType);
      }

    } catch (ModuleException e) {
      throw e;
    } catch (Exception e) {
      throw new ModuleException("Error while generating embeddings with \"EINSTEIN\" embedding model service.",
          MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }
}
