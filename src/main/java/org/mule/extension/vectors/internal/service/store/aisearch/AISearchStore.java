package org.mule.extension.vectors.internal.service.store.aisearch;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import com.azure.core.exception.HttpResponseException;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;

public class AISearchStore extends BaseStoreService {

  private final String apiKey;
  private final String url;
  private final QueryParameters queryParams;
  private final AISearchStoreConnection aiSearchStoreConnection;


  public AISearchStore(StoreConfiguration compositeConfiguration, AISearchStoreConnection aiSearchStoreConnection,
                       String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    super(compositeConfiguration, aiSearchStoreConnection, storeName, dimension, createStore);
    this.url = aiSearchStoreConnection.getUrl();
    this.apiKey = aiSearchStoreConnection.getApiKey();
    this.queryParams = queryParams;
    this.aiSearchStoreConnection = aiSearchStoreConnection;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      if (createStore && dimension == 0) {
        throw new ModuleException("Dimension cannot be 0 when creating or updating index", MuleVectorsErrorType.INVALID_REQUEST);
      }
      return AzureAiSearchEmbeddingStore.builder()
          .endpoint(url)
          .apiKey(apiKey)
          .indexName(storeName)
          // Default dimension 1536. Required but ignored since used only for delete and query all operations
          .dimensions(dimension > 0 ? dimension : 1536)
          .createOrUpdateIndex(createStore)
          .filterMapper(new VectorsAzureAiSearchFilterMapper())
          .build();
    } catch (HttpResponseException e) {
      switch (e.getResponse().getStatusCode()) {
        case 401:
        case 403:
          throw new ModuleException("Authentication failed: " + e.getMessage(), MuleVectorsErrorType.INVALID_CONNECTION, e);
        case 400:
          throw new ModuleException("Invalid request to Azure AI Search: " + e.getMessage(), MuleVectorsErrorType.INVALID_REQUEST,
                                    e);
        default:
          throw new ModuleException("Azure AI Search service error: " + e.getMessage(), MuleVectorsErrorType.SERVICE_ERROR, e);
      }
    } catch (Exception e) {
      throw new ModuleException("Failed to build Azure AI Search embedding store: " + e.getMessage(),
                                MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public AISearchStoreIterator<?> getFileIterator() {
    return new AISearchStoreIterator<>(
                                       this.storeName,
                                       this.queryParams,
                                       this.aiSearchStoreConnection);
  }
}
