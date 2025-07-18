package org.mule.extension.vectors.internal.operation;

import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.StoreResponseAttributes;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.StoreErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.parameter.CustomMetadata;
import org.mule.extension.vectors.internal.helper.parameter.RemoveFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.store.StoreOperationsHelper;
import org.mule.extension.vectors.internal.metadata.RowsOutputTypeMetadataResolver;
import org.mule.extension.vectors.internal.pagination.RowPagingProvider;
import org.mule.extension.vectors.internal.service.store.VectorStoreService;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.fixed.InputJsonType;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import static org.mule.extension.vectors.internal.helper.store.StoreOperationsHelper.executeStoreOperation;
import static org.mule.extension.vectors.internal.helper.store.StoreOperationsHelper.parseStoreInput;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;
import static org.mule.sdk.api.annotation.param.MediaType.ANY;

/**
 * Class providing operations for embedding store management including querying, adding, removing, and listing sources.
 */
public class StoreOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(StoreOperations.class);

  /**
   * Queries an embedding store based on the provided embedding and text segment, and applies a metadata filter.
   *
   * @param storeConfiguration the configuration of the store
   * @param storeConnection    the connection to the store
   * @param storeName          the name of the store to query
   * @param content            the input stream containing the text segment and embedding
   * @param maxResults         the maximum number of results to retrieve
   * @param minScore           the minimum score to filter results
   * @param searchFilterParams the search filter parameters
   * @return a result containing the store response
   * @throws ModuleException if an error occurs during the operation
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Query")
  @DisplayName("[Store] Query")
  @Throws(StoreErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/StoreQueryResponse.json")
  public Result<InputStream, StoreResponseAttributes> query(
      @Config StoreConfiguration storeConfiguration,
      @Connection BaseStoreConnection storeConnection,
      @Alias("storeName") @Summary("Name of the store/collection to query.") String storeName,
      @Alias("textSegmentAndEmbedding")
          @Summary("Text Segment and Embedding generated from question and used to query the store.")
          @DisplayName("Text Segment and Embedding")
          @InputJsonType(schema = "api/metadata/EmbeddingGenerateResponse.json")
          @Content InputStream content,
      @Alias("maxResults") @Summary("Maximum number of results (text segments) retrieved.") Integer maxResults,
      @Alias("minScore") @Summary("Minimum score used to filter retrieved results (text segments).") Double minScore,
      @ParameterGroup(name = "Filter") SearchFilterParameters searchFilterParams) {

    final double finalMinScore = (minScore == null) ? Constants.EMBEDDING_SEARCH_REQUEST_DEFAULT_MIN_SCORE : minScore;

    var input = parseStoreInput(content, false, null);

    Function<VectorStoreService, JSONObject> operation =
        (storeService) -> storeService.query(input.textSegments(), input.embeddings(), maxResults, finalMinScore, searchFilterParams);


    return executeStoreOperation(
        new StoreOperationsHelper.StoreOperationContext(
            storeConfiguration, storeConnection, storeName, input.dimension(), false, null,
            createStoreNameAndSearchFilterMap(storeName, searchFilterParams)),
        operation,
        (response) -> response
    );
  }

  /**
   * Lists all sources in the specified embedding store.
   *
   * @param storeConfiguration the configuration of the store
   * @param storeName          the name of the store
   * @param queryParams        the query parameters for listing sources
   * @return a result containing the store response with metadata of sources
   * @throws ModuleException if an error occurs during the operation
   */
  @MediaType(value = ANY, strict = false)
  @Alias("Query-all")
  @DisplayName("[Store] Query all")
  @Throws(StoreErrorTypeProvider.class)
  @OutputResolver(output = RowsOutputTypeMetadataResolver.class)
  public PagingProvider<BaseStoreConnection, Result<CursorProvider<Cursor>, StoreResponseAttributes>> queryAll(
      @Config StoreConfiguration storeConfiguration,
      String storeName,
      @ParameterGroup(name = "Query Parameters") QueryParameters queryParams,
      StreamingHelper streamingHelper) {

    try {

      return new RowPagingProvider(storeConfiguration,
                                   storeName,
                                   queryParams,
                                   streamingHelper);

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while listing sources from the store %s", storeName),
          MuleVectorsErrorType.STORE_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Adds embeddings and text segments to the store.
   *
   * @param storeConfiguration the configuration of the store
   * @param storeConnection    the connection to the store
   * @param storeName          the name of the store to add data to
   * @param content            the input stream containing the text segments and embeddings
   * @return a result containing the store response
   * @throws ModuleException if an error occurs during the operation
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Store-add")
  @DisplayName("[Store] Add")
  @Throws(StoreErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/StoreAddResponse.json")
  public Result<InputStream, StoreResponseAttributes> addToStore(
      @Config StoreConfiguration storeConfiguration,
      @Connection BaseStoreConnection storeConnection,
      @Alias("storeName") @Summary("Name of the store/collection to use for data ingestion.") String storeName,
      @Alias("textSegmentsAndEmbeddings")
          @DisplayName("Text Segments and Embeddings")
          @InputJsonType(schema = "api/metadata/EmbeddingGenerateResponse.json")
          @Content InputStream content,
      @ParameterGroup(name="Custom Metadata") CustomMetadata customMetadata) {

    var input = parseStoreInput(content, true, customMetadata);

    Function<VectorStoreService, List<String>> operation = (storeService) -> {
      List<String> ids = storeService.add(input.embeddings(), input.textSegments());
      String sourceDisplayName = MetadataUtils.getSourceDisplayName(input.textSegments().get(0).metadata());
      LOGGER.info("Ingested into {}  >> {}", storeName, sourceDisplayName);
      return ids;
    };

    Function<List<String>, JSONObject> responseBuilder = (ids) -> JsonUtils.createIngestionStatusObject(
        input.ingestionMetadata().get(Constants.METADATA_KEY_SOURCE_ID).toString(), ids);

    return executeStoreOperation(
        new StoreOperationsHelper.StoreOperationContext(
            storeConfiguration, storeConnection, storeName, input.dimension(), true, null,
            createStoreNameMap(storeName)),
        operation,
        responseBuilder
    );
  }

  /**
   * Removes embeddings from the store based on the provided filter.
   *
   * @param storeConfiguration the configuration of the store
   * @param storeConnection    the connection to the store
   * @param storeName          the name of the store
   * @param removeFilterParams the filter parameters for removal
   * @return a result containing the store response
   * @throws ModuleException if an error occurs during the operation
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Store-remove")
  @DisplayName("[Store] Remove")
  @Throws(StoreErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/StoreRemoveFromStoreResponse.json")
  public Result<InputStream, StoreResponseAttributes> remove(
      @Config StoreConfiguration storeConfiguration,
      @Connection BaseStoreConnection storeConnection,
      String storeName,
      @ParameterGroup(name = "Filter") RemoveFilterParameters removeFilterParams) {

    removeFilterParams.validate();

    Function<VectorStoreService, Void> operation = (storeService) -> {
      storeService.remove(removeFilterParams);
      return null;
    };

    Function<Void, JSONObject> responseBuilder = (v) -> new JSONObject().put(Constants.JSON_KEY_STATUS, Constants.OPERATION_STATUS_DELETED);

    return executeStoreOperation(
        new StoreOperationsHelper.StoreOperationContext(
            storeConfiguration, storeConnection, storeName, 0, false, null,
            createStoreNameAndRemoveFilterMap(storeName, removeFilterParams)),
        operation,
        responseBuilder
    );
  }

  private static HashMap<String, Object> createStoreNameAndSearchFilterMap(String storeName, Object searchFilterParams) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("storeName", storeName);
    map.put("searchFilter", searchFilterParams);
    return map;
  }

  private static HashMap<String, Object> createStoreNameMap(String storeName) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("storeName", storeName);
    return map;
  }

  private static HashMap<String, Object> createStoreNameAndRemoveFilterMap(String storeName, Object removeFilterParams) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("storeName", storeName);
    map.put("removeFilter", removeFilterParams);
    return map;
  }
}
