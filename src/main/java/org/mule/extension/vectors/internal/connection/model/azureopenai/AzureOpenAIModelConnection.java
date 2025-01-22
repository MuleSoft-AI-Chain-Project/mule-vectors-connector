package org.mule.extension.vectors.internal.connection.model.azureopenai;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.Header;
import com.azure.core.util.HttpClientOptions;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AzureOpenAIModelConnection implements BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIModelConnection.class);

  private String endpoint;
  private String apiKey;
  private OpenAIClient openAIClient;

  public AzureOpenAIModelConnection(String endpoint, String apiKey) {
    this.endpoint = endpoint;
    this.apiKey = apiKey;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getApiKey() {
    return apiKey;
  }

  public OpenAIClient getOpenAIClient() {
    return openAIClient;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI;
  }

  @Override
  public void connect() throws ConnectionException {

    Duration timeout = Duration.ofSeconds(60L);
    HttpClientOptions clientOptions = new HttpClientOptions();
    clientOptions.setConnectTimeout(timeout);
    clientOptions.setResponseTimeout(timeout);
    clientOptions.setReadTimeout(timeout);
    clientOptions.setWriteTimeout(timeout);
    String userAgent = "langchain4j-azure-openai";

    List<Header> headers = new ArrayList();
    headers.add(new Header("User-Agent", userAgent));
    clientOptions.setHeaders(headers);

    HttpClient httpClient = (new NettyAsyncHttpClientProvider()).createInstance(clientOptions);
    HttpLogOptions httpLogOptions = new HttpLogOptions();

    ExponentialBackoffOptions exponentialBackoffOptions = new ExponentialBackoffOptions();
    exponentialBackoffOptions.setMaxRetries(3);
    RetryOptions retryOptions = new RetryOptions(exponentialBackoffOptions);
    AzureKeyCredential credential = new AzureKeyCredential(apiKey);
    OpenAIClientBuilder openAIClientBuilder = (new OpenAIClientBuilder())
        .endpoint(endpoint)
        .credential(credential)
        .serviceVersion(OpenAIServiceVersion.getLatest())
        .httpClient(httpClient).clientOptions(clientOptions)
        .httpLogOptions(httpLogOptions)
        .retryOptions(retryOptions);

    this.openAIClient = openAIClientBuilder.buildClient();

    LOGGER.debug("Connected to Azure Open AI.");
  }

  @Override
  public void disconnect() {

    if(this.openAIClient != null) {

      // Add logic to invalidate connection
      LOGGER.debug("Disconnecting from Azure Open AI.");
    }
  }

  @Override
  public boolean isValid() {

    openAIClient.listBatches();
    LOGGER.debug("Azure Open AI connection is valid.");
    return true;
  }
}
