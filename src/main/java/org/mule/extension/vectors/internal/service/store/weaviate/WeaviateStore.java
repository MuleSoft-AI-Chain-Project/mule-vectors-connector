package org.mule.extension.vectors.internal.service.store.weaviate;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.Optional;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;

/**
 * WeaviateStore is a specialized implementation of {@link BaseStoreService} designed to interact with
 * the Weaviate database for managing vector data and sources.
 */
public class WeaviateStore extends BaseStoreService {

  private final QueryParameters queryParams;

  /**
   * Initializes a new instance of WeaviateStore.
   *
   * @param storeConfiguration the configuration object containing necessary settings.
   * @param storeName          the name of the vector store.
   * @param queryParams        parameters related to query configurations.
   */
  public WeaviateStore(StoreConfiguration storeConfiguration, WeaviateStoreConnection weaviateStoreConnection, String storeName,
                       QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, weaviateStoreConnection, storeName, dimension, createStore);

    this.queryParams = queryParams;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      return WeaviateEmbeddingStore.builder()
          .apiKey(((WeaviateStoreConnection) storeConnection).getApikey())
          .scheme(((WeaviateStoreConnection) storeConnection).getScheme())
          .host(((WeaviateStoreConnection) storeConnection).getHost())
          .port(((WeaviateStoreConnection) storeConnection).getPort())
          .securedGrpc(((WeaviateStoreConnection) storeConnection).isSecuredGrpc())
          .grpcPort(((WeaviateStoreConnection) storeConnection).getGrpcPort())
          .useGrpcForInserts(((WeaviateStoreConnection) storeConnection).isUseGrpcForInserts())
          .avoidDups(((WeaviateStoreConnection) storeConnection).isAvoidDups())
          .consistencyLevel(((WeaviateStoreConnection) storeConnection).getConsistencyLevel())
          .objectClass(storeName)
          .build();
    } catch (AuthenticationException e) {
      String errorMessage = Optional.of(e)
          .map(Throwable::getCause)
          .map(Throwable::getMessage)
          .orElse(e.getMessage());
      throw new ModuleException("Authentication failed: " + errorMessage, MuleVectorsErrorType.INVALID_CONNECTION, e);
    } catch (Exception e) {
      throw new ModuleException("Failed to build Weaviate embedding store: " + e.getMessage(),
                                MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public WeaviateStoreIterator<?> getFileIterator() {
    return new WeaviateStoreIterator<>(
                                       this.queryParams);
  }
}
