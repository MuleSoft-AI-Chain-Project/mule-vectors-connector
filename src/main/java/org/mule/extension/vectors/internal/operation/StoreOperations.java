package org.mule.extension.vectors.internal.operation;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.StoreResponseAttributes;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.StoreErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.model.EmbeddingOperationValidator;
import org.mule.extension.vectors.internal.helper.parameter.CustomMetadata;
import org.mule.extension.vectors.internal.helper.parameter.MetadataFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.InputJsonType;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.*;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static org.mule.extension.vectors.internal.helper.ResponseHelper.*;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

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
      @Alias("maxResults") @Summary("Maximum number of results (text segments) retrieved.") Number maxResults,
      @Alias("minScore") @Summary("Minimum score used to filter retrieved results (text segments).") Double minScore,
      @ParameterGroup(name = "Filter") MetadataFilterParameters.SearchFilterParameters searchFilterParams) {

    List<TextSegment> textSegments = new LinkedList<>();
    List<Embedding> embeddings = new LinkedList<>();
    int dimension;

    try {

      int maximumResults = maxResults.intValue();
      if (minScore == null) { minScore = Constants.EMBEDDING_SEARCH_REQUEST_DEFAULT_MIN_SCORE; }

      try {

        String contentString = IOUtils.toString(content, StandardCharsets.UTF_8);
        JSONObject jsonContent = new JSONObject(contentString);

        if (jsonContent.has(Constants.JSON_KEY_TEXT_SEGMENTS)) {

          JSONArray jsonTextSegments = jsonContent.getJSONArray(Constants.JSON_KEY_TEXT_SEGMENTS);
          IntStream.range(0, jsonTextSegments.length())
              .mapToObj(jsonTextSegments::getJSONObject) // Convert index to JSONObject
              .forEach(jsonTextSegment -> {
                HashMap<String, Object> metadataMap =
                    (HashMap<String, Object>) jsonTextSegment.getJSONObject(Constants.JSON_KEY_METADATA).toMap();
                Metadata metadata = Metadata.from(metadataMap);
                textSegments.add(new TextSegment(jsonTextSegment.getString(Constants.JSON_KEY_TEXT), metadata));
              });

          if (jsonTextSegments.length() != 1) {

            throw new ModuleException(
                String.format("You must provide one text segment only. Received: %s", String.valueOf(jsonTextSegments.length())),
                MuleVectorsErrorType.INVALID_PARAMETERS_ERROR);
          }
        }

        JSONArray jsonEmbeddings = jsonContent.getJSONArray(Constants.JSON_KEY_EMBEDDINGS);
        IntStream.range(0, jsonEmbeddings.length())
            .mapToObj(jsonEmbeddings::getJSONArray) // Convert index to JSONObject
            .forEach(jsonEmbedding -> {

              // Convert JSONArray to float[]
              float[] floatArray = new float[jsonEmbedding.length()];
              for (int i = 0; i < jsonEmbedding.length(); i++) {
                floatArray[i] = (float) jsonEmbedding.getDouble(i);
              }
              embeddings.add(new Embedding(floatArray));
            });

        if(embeddings.size() != 1) {

          throw new ModuleException(String.format("You must provide one embedding only. Received: %s", String.valueOf(embeddings.size())),
                                    MuleVectorsErrorType.INVALID_PARAMETERS_ERROR);
        }

        dimension = jsonContent.getInt(Constants.JSON_KEY_DIMENSION);
        ValidationUtils.ensureGreaterThanZero(dimension, Constants.JSON_KEY_DIMENSION);

      } catch (Exception e) {

        throw new ModuleException(
            String.format("Error while parsing Text Segments and Embeddings input."),
            MuleVectorsErrorType.INVALID_PARAMETERS_ERROR,
            e);
      }

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .connection(storeConnection)
          .dimension(dimension)
          .createStore(false)
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      EmbeddingSearchRequest.EmbeddingSearchRequestBuilder searchRequestBuilder = EmbeddingSearchRequest.builder()
          .queryEmbedding(embeddings.get(0))
          .maxResults(maximumResults)
          .minScore(minScore);

      JSONObject jsonObject = new JSONObject();

      if(searchFilterParams.areFilterParamsSet()) {

        EmbeddingOperationValidator.validateOperationType(
            Constants.STORE_OPERATION_TYPE_FILTER_BY_METADATA, storeConnection.getVectorStore());
        Filter filter = searchFilterParams.buildMetadataFilter();
        searchRequestBuilder.filter(filter);
      }

      EmbeddingSearchRequest searchRequest = searchRequestBuilder.build();

      EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
      List<EmbeddingMatch<TextSegment>> embeddingMatches = searchResult.matches();

      String information = embeddingMatches.stream()
          .map(match -> match.embedded().text())
          .collect(joining("\n\n"));

      jsonObject.put(Constants.JSON_KEY_RESPONSE, information);
      jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);
      if(textSegments.size() == 1) jsonObject.put(Constants.JSON_KEY_QUESTION, textSegments.get(0).text());
      jsonObject.put(Constants.JSON_KEY_MAX_RESULTS, maxResults);
      jsonObject.put(Constants.JSON_KEY_MIN_SCORE, minScore);

      JSONArray sources = new JSONArray();

      JSONObject contentObject;
      for (EmbeddingMatch<TextSegment> match : embeddingMatches) {

        Metadata matchMetadata = match.embedded().metadata();
        contentObject = new JSONObject();
        contentObject.put(Constants.JSON_KEY_EMBEDDING_ID, match.embeddingId());
        contentObject.put(Constants.JSON_KEY_TEXT, match.embedded().text());
        contentObject.put(Constants.JSON_KEY_SCORE, match.score());
        JSONObject metadataObject = new JSONObject(matchMetadata.toMap());
        contentObject.put(Constants.JSON_KEY_METADATA, metadataObject);
        sources.put(contentObject);
      }

      jsonObject.put(Constants.JSON_KEY_SOURCES, sources);

      return createStoreResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
            put("searchFilter", searchFilterParams);
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while querying embeddings from the store %s", storeName),
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

    try {

      String contentString = IOUtils.toString(content, StandardCharsets.UTF_8);

      JSONObject jsonContent = new JSONObject(contentString);

      HashMap<String, Object> ingestionMetadataMap = MetadataUtils.getIngestionMetadata();

      JSONArray jsonTextSegments = jsonContent.getJSONArray(Constants.JSON_KEY_TEXT_SEGMENTS);
      List<TextSegment> textSegments = new LinkedList<>();
      IntStream.range(0, jsonTextSegments.length())
          .mapToObj(jsonTextSegments::getJSONObject) // Convert index to JSONObject
          .forEach(jsonTextSegment -> {
            HashMap<String, Object> metadataMap = (HashMap<String, Object>)jsonTextSegment.getJSONObject(Constants.JSON_KEY_METADATA).toMap();
            metadataMap.putAll(ingestionMetadataMap);
            if(customMetadata != null && customMetadata.getMetadataEntries() != null) metadataMap.putAll(customMetadata.getMetadataEntries());
            Metadata metadata = Metadata.from(metadataMap);
            textSegments.add(new TextSegment(jsonTextSegment.getString(Constants.JSON_KEY_TEXT), metadata));
          });

      JSONArray jsonEmbeddings = jsonContent.getJSONArray(Constants.JSON_KEY_EMBEDDINGS);
      List<Embedding> embeddings = new LinkedList<>();
      IntStream.range(0, jsonEmbeddings.length())
          .mapToObj(jsonEmbeddings::getJSONArray) // Convert index to JSONObject
          .forEach(jsonEmbedding -> {

            // Convert JSONArray to float[]
            float[] floatArray = new float[jsonEmbedding.length()];
            for (int i = 0; i < jsonEmbedding.length(); i++) {
              floatArray[i] = (float) jsonEmbedding.getDouble(i);
            }
            embeddings.add(new Embedding(floatArray));
          });

      int dimension = jsonContent.getInt(Constants.JSON_KEY_DIMENSION);
      ValidationUtils.ensureGreaterThanZero(dimension, Constants.JSON_KEY_DIMENSION);

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(storeConfiguration)
          .connection(storeConnection)
          .dimension(dimension)
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      try {
        embeddingStore.addAll(embeddings, textSegments);
        LOGGER.info(String.format("Ingested into %s  >> %s",
                                  storeName,
                                  MetadataUtils.getSourceDisplayName(textSegments.get(0).metadata())));

      } catch(Exception e) {

        throw new ModuleException(
            String.format("Error while adding data to store \"%s\"", storeName),
            MuleVectorsErrorType.STORE_SERVICES_FAILURE,
            e);
      }

      JSONObject jsonObject = JsonUtils.createIngestionStatusObject(storeName);

      return createStoreResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while adding data to store \"%s\"", storeName),
          MuleVectorsErrorType.STORE_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Lists all sources in the specified embedding store.
   *
   * @param storeConfiguration the configuration of the store
   * @param storeConnection    the connection to the store
   * @param storeName          the name of the store
   * @param queryParams        the query parameters for listing sources
   * @return a result containing the store response with metadata of sources
   * @throws ModuleException if an error occurs during the operation
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Store-list-sources")
  @DisplayName("[Store] List sources")
  @Throws(StoreErrorTypeProvider.class)
  @OutputJsonType(schema = "api/metadata/StoreListSourcesResponse.json")
  public Result<InputStream, StoreResponseAttributes> listSources(
      @Config StoreConfiguration storeConfiguration,
      @Connection BaseStoreConnection storeConnection,
      String storeName,
      @ParameterGroup(name = "Querying Strategy") QueryParameters queryParams) {

    try {

      EmbeddingOperationValidator.validateOperationType(
          Constants.STORE_OPERATION_TYPE_QUERY_ALL, storeConnection.getVectorStore());
      EmbeddingOperationValidator.validateOperationType(
          Constants.STORE_OPERATION_TYPE_FILTER_BY_METADATA, storeConnection.getVectorStore());

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(storeConfiguration)
          .connection(storeConnection)
          .queryParams(queryParams)
          .createStore(false)
          .build();

      JSONObject jsonObject = baseStore.listSources();

      return createStoreResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
          }});

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
  public Result<InputStream, StoreResponseAttributes> removeEmbeddings(
      @Config StoreConfiguration storeConfiguration,
      @Connection BaseStoreConnection storeConnection,
      String storeName,
      @ParameterGroup(name = "Filter") MetadataFilterParameters.RemoveFilterParameters removeFilterParams) {

    try {
      EmbeddingOperationValidator.validateOperationType(
          Constants.STORE_OPERATION_TYPE_REMOVE_EMBEDDINGS, storeConnection.getVectorStore());
      EmbeddingOperationValidator.validateOperationType(
          Constants.STORE_OPERATION_TYPE_FILTER_BY_METADATA, storeConnection.getVectorStore());

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(storeConfiguration)
          .connection(storeConnection)
          .createStore(false)
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      Filter filter = removeFilterParams.buildMetadataFilter();

      embeddingStore.removeAll(filter);

      JSONObject jsonObject = new JSONObject();
      jsonObject.put(Constants.JSON_KEY_STATUS, Constants.OPERATION_STATUS_DELETED);

      return createStoreResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
            put("removeFilter", removeFilterParams);
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while removing embeddings from the store %s", storeName),
          MuleVectorsErrorType.STORE_OPERATIONS_FAILURE,
          e);
    }
  }
}
