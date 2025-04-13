package org.mule.extension.vectors.internal.connection.model.huggingface;

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

@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnection implements BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuggingFaceModelConnection.class);

  private static final String ENDPOINT = "https://huggingface.co/api/whoami-v2";

  private String apiKey;

  public HuggingFaceModelConnection(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE;
  }

  @Override
  public void connect() throws ConnectionException {

    try {

      doAuthenticatedHttpRequest();
    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Hugging Face.", e);
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

      LOGGER.error("Failed to validate connection to Hugging Face.", e);
      return false;
    }
  }

  private void doAuthenticatedHttpRequest() throws ConnectionException {

    try {

      // Set up the HTTP connection
      URL url = new URL(ENDPOINT);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Authorization", "Bearer " + apiKey);

      // Check the response code
      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        // Error handling
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        String line;
        StringBuilder errorResponse = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          errorResponse.append(line);
        }
        reader.close();
        // Print the error response
        LOGGER.error("Error (HTTP " + responseCode + "): " + errorResponse.toString());
        throw new ConnectionException("Impossible to connect to Hugging Face. " + "Error (HTTP " + responseCode + "): " + errorResponse.toString());
      }

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      LOGGER.error("Impossible to connect to Hugging Face", e);
      throw new ConnectionException("Impossible to connect to Hugging Face", e);
    }
  }
}
