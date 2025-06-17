package org.mule.extension.vectors.internal.connection.store.aisearch;

import com.azure.search.documents.SearchServiceVersion;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.http.api.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AISearchStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(AISearchStoreConnection.class);
  
  private String url;
  private String apiKey;
  private final AISearchStoreConnectionParameters parameters;
  private HttpClient httpClient;

  public AISearchStoreConnection(AISearchStoreConnectionParameters parameters, HttpClient httpClient) {
    this.parameters = parameters;
    this.url = parameters.getUrl();
    this.apiKey = parameters.getApiKey();
    this.httpClient = httpClient;
  }

  public String getUrl() {
    return url;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_AI_SEARCH;
  }

  @Override
  public void disconnect() {

  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    try {
      if (url == null ) {
        throw new IllegalArgumentException("URL is required for AI Search connection.");
      }
      if (apiKey == null) {
        throw new IllegalArgumentException("API Key is required for AI Search connection.");
      }
      // return true;
    } catch (Exception e) {
      LOGGER.error("Failed to validate connection to AI Search.", e);
      // return false;
    }
  }

  private void doAuthenticatedHttpRequest() throws ConnectionException {

    try {

      // Construct the endpoint URL
      String endpoint = url + "?api-version=" + 
          SearchServiceVersion.getLatest().toString().substring(1).replace("_", "-");

      // Create URL object
      URL url = new URL(endpoint);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set request method to GET
      connection.setRequestMethod("GET");

      // Set headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("api-key", apiKey);

      // Get the response code
      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        // Read error response
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
          String inputLine;
          StringBuilder response = new StringBuilder();
          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          // Print the error response
          LOGGER.error("Error (HTTP " + responseCode + "): " + response.toString());
          throw new ConnectionException("Impossible to connect to AI Search. " + "Error (HTTP " + responseCode + "): " + response.toString());
        }
      }

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      LOGGER.error("Impossible to connect to AI Search", e);
      throw new ConnectionException("Impossible to connect to AI Search", e);
    }
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }
}
