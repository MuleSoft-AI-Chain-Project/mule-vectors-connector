package org.mule.extension.vectors.internal.connection.model.ollama;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Alias("ollama")
@DisplayName("Ollama")
public class OllamaModelConnection implements BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(OllamaModelConnection.class);

  private String baseUrl;
  private int maxAttempts;
  private long timeout;

  public OllamaModelConnection(String baseUrl, int maxAttempts, long timeout) {

    this.baseUrl = baseUrl;
    this.maxAttempts = maxAttempts;
    this.timeout = timeout;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public int getMaxAttempts() { return maxAttempts;}

  public long getTimeout() { return timeout; }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_OLLAMA;
  }

  @Override
  public void connect() throws ConnectionException {

    try {

      doHttpRequest();
    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Ollama.", e);
    }
  }

  @Override
  public void disconnect() {

  }

  @Override
  public boolean isValid() {

    try {

      doHttpRequest();
      return true;

    } catch (Exception e) {

      LOGGER.error("Failed to validate connection to Ollama.", e);
      return false;
    }
  }

  private void doHttpRequest() throws ConnectionException {

    try {

      // Create the URL object
      URL url = new URL(baseUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set request method to POST
      connection.setRequestMethod("GET");

      // Check the response code
      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        // Read the error response
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
          String inputLine;
          StringBuilder response = new StringBuilder();
          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          // Print the error response
          LOGGER.error("Error (HTTP " + responseCode + "): " + response.toString());
          throw new ConnectionException("Impossible to connect to Ollama. " + "Error (HTTP " + responseCode + "): " + response.toString());
        }
      }

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      LOGGER.error("Impossible to connect to Ollama", e);
      throw new ConnectionException("Impossible to connect to Ollama", e);
    }
  }
}
