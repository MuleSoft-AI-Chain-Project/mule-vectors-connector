package org.mule.extension.vectors.internal.connection.model.nomic;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Alias("nomic")
@DisplayName("Nomic")
public class NomicModelConnection implements BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(NomicModelConnection.class);

  private static final String ENDPOINT = "https://api-atlas.nomic.ai/v1/embedding/text";

  private String apiKey;

  private int maxAttempts;
  private long timeout;

  public NomicModelConnection(String apiKey, int maxAttempts, long timeout) {

    this.apiKey = apiKey;
    this.maxAttempts = maxAttempts;
    this.timeout = timeout;
  }

  public String getApiKey() {
    return apiKey;
  }

  public int getMaxAttempts() { return maxAttempts;}

  public long getTimeout() { return timeout; }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_NOMIC;
  }

  @Override
  public void connect() throws ConnectionException {

    if(apiKey.compareTo("demo") != 0) {

      try {

        doAuthenticatedHttpRequest();
      } catch (ConnectionException e) {

        throw e;

      } catch (Exception e) {

        throw new ConnectionException("Failed to connect to Nomic.", e);
      }
    }
  }

  @Override
  public void disconnect() {

  }

  @Override
  public boolean isValid() {

    try {

      doAuthenticatedHttpRequest();
      return true;

    } catch (Exception e) {

      LOGGER.error("Failed to validate connection to Nomic.", e);
      return false;
    }
  }

  private void doAuthenticatedHttpRequest() throws ConnectionException {

    try {

      // Send empty array of text to avoid token usage ("usage":{"prompt_tokens":0,"total_tokens":0})
      String payload = "{"
          + "\"texts\": [],"
          + "\"task_type\": \"search_document\","
          + "\"max_tokens_per_text\": 0,"
          + "\"dimensionality\": 768"
          + "}";

      // Create the URL object
      URL url = new URL(ENDPOINT);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      // Set request method to POST
      connection.setRequestMethod("POST");

      // Set headers
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Accept", "application/json");
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);

      // Enable writing to the connection output stream
      connection.setDoOutput(true);

      // Write the JSON payload
      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = payload.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

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
          throw new ConnectionException("Impossible to connect to Nomic. " + "Error (HTTP " + responseCode + "): " + response.toString());
        }
      }

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      LOGGER.error("Impossible to connect to Nomic", e);
      throw new ConnectionException("Impossible to connect to Nomic", e);
    }
  }
}
