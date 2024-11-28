package org.mule.extension.vectors.internal.operation;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createEmbeddingResponse;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import org.mule.extension.vectors.api.metadata.EmbeddingResponseAttributes;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.provider.EmbeddingErrorTypeProvider;
import org.mule.extension.vectors.internal.helper.EmbeddingOperationValidator;
import org.mule.extension.vectors.internal.helper.parameter.*;
import org.mule.extension.vectors.internal.config.Configuration;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.storage.BaseStorageConfiguration;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.MetadatatUtils;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.annotation.param.*;

import static java.util.stream.Collectors.joining;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class EmbeddingOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddingOperations.class);

  /**
   * Adds Text to Embedding Store
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-add-text-to-store")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingAddToStoreResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      addTextToStore( @Config Configuration configuration,
                      @Alias("text") @DisplayName("Text") String text,
                      @Alias("storeName") @DisplayName("Store Name")  String storeName,
                      @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters,
                      @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters){

    try {

      LOGGER.debug(String.format("Adding text %s to store %s", text, storeName));

      BaseModel baseModel = BaseModel.builder()
          .configuration(configuration)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(configuration)
          .dimension(embeddingModel.dimension())
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
          .documentSplitter(DocumentSplitters.recursive(segmentationParameters.getMaxSegmentSizeInChar(), segmentationParameters.getMaxOverlapSizeInChars()))
          .embeddingModel(embeddingModel)
          .embeddingStore(embeddingStore)
          .build();

      Document document = new Document(text);
      MetadatatUtils.addMetadataToDocument(document, Constants.FILE_TYPE_TEXT);

      embeddingStoreIngestor.ingest(document);

      JSONObject jsonObject = JsonUtils.createIngestionStatusObject(storeName);

      return createEmbeddingResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while adding text \"%s\" into the store %s", text, storeName),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }

   /**
   * Adds Text to Embedding Store
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-generate-from-text")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingGenerateFromTextResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      generateEmbedding(@Config Configuration configuration,
                        @Alias("text") @DisplayName("Text")  String text,
                        @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters,
                        @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters){

    try {

      BaseModel baseModel = BaseModel.builder()
          .configuration(configuration)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      DocumentSplitter documentSplitter = DocumentSplitters.recursive(segmentationParameters.getMaxSegmentSizeInChar(),
                                                                      segmentationParameters.getMaxOverlapSizeInChars());

      List<TextSegment> textSegments = documentSplitter.split(new Document(text));
      List<Embedding> embeddings;
      try {

        embeddings = embeddingModel.embedAll(textSegments).content();

      } catch(Exception e) {

        throw new ModuleException(
            String.format("Error while generating embedding from text \"%s\"", text),
            MuleVectorsErrorType.AI_SERVICES_FAILURE,
            e);
      }

      JSONObject jsonObject = new JSONObject();
      JSONArray jsonSegments = IntStream.range(0, embeddings.size())
          .mapToObj(i -> {
            JSONObject jsonSegment = new JSONObject();
            jsonSegment.put(Constants.JSON_KEY_TEXT, textSegments.get(i).text());
            jsonSegment.put(Constants.JSON_KEY_EMBEDDING, Arrays.toString(embeddings.get(i).vector())); // Replace getText with the actual method
            jsonSegment.put(Constants.JSON_KEY_INDEX, i);
            return jsonSegment;
          })
          .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);

      jsonObject.put(Constants.JSON_KEY_SEGMENTS, jsonSegments);
      jsonObject.put(Constants.JSON_KEY_DIMENSIONS, embeddingModel.dimension());

      return createEmbeddingResponse(jsonObject.toString(), new HashMap<>());

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while generating embedding from text \"%s\"", text),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Loads multiple files from a folder into the embedding store. URLs are not supported with this operation.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("Embedding-add-folder-to-store")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingAddToStoreResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      addFolderToStore( @Config Configuration configuration,
                        @Alias("storeName") @DisplayName("Store Name") String storeName,
                        @ConfigOverride @Alias("storage") @DisplayName(Constants.PARAM_DISPLAY_NAME_STORAGE_OVERRIDE)
                        BaseStorageConfiguration storageConfiguration,
                        @ParameterGroup(name = "Documents") DocumentParameters documentParameters,
                        @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters,
                        @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters){

    try {

      EmbeddingOperationValidator.validateOperationType(
              Constants.EMBEDDING_OPERATION_TYPE_STORE_METADATA,configuration.getStoreConfiguration().getVectorStore());

      BaseModel baseModel = BaseModel.builder()
          .configuration(configuration)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(configuration)
          .dimension(embeddingModel.dimension())
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
          .documentSplitter(DocumentSplitters.recursive(segmentationParameters.getMaxSegmentSizeInChar(), segmentationParameters.getMaxOverlapSizeInChars()))
          .embeddingModel(embeddingModel)
          .embeddingStore(embeddingStore)
          .build();

      BaseStorage baseStorage = BaseStorage.builder()
          .storageConfiguration(storageConfiguration)
          .contextPath(documentParameters.getContextPath())
          .fileType(documentParameters.getFileType())
          .build();

      long documentNumber = 0L;
      while(baseStorage.hasNext()) {

        Document document = baseStorage.next();
        embeddingStoreIngestor.ingest(document);
        documentNumber ++;
      }
      JSONObject jsonObject = JsonUtils.createIngestionStatusObject(storeName);

      long finalDocumentNumber = documentNumber;
      return createEmbeddingResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("documentCount", finalDocumentNumber);
            put("storeName", storeName);
            put("storageType", baseStorage.getStorageType());
            put("fileType", documentParameters.getFileType());
            put("contextPath", documentParameters.getContextPath());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while adding folder %s into the store %s", documentParameters.getContextPath(), storeName),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }

    /**
   * Add document of type text, pdf and url to embedding store, provide the storeName (Index, Collection, etc).
     * @throws InterruptedException 
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("EMBEDDING-add-document-to-store")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingAddToStoreResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      addFileEmbedding( @Config Configuration configuration,
                        @Alias("storeName") @DisplayName("Store Name") String storeName,
                        @ConfigOverride @Alias("storage") @DisplayName(Constants.PARAM_DISPLAY_NAME_STORAGE_OVERRIDE)
                            BaseStorageConfiguration storageConfiguration,
                        @ParameterGroup(name = "Document")  DocumentParameters documentParameters,
                        @ParameterGroup(name = "Segmentation") SegmentationParameters segmentationParameters,
                        @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {

      EmbeddingOperationValidator.validateOperationType(
              Constants.EMBEDDING_OPERATION_TYPE_STORE_METADATA,configuration.getStoreConfiguration().getVectorStore());

      BaseModel baseModel = BaseModel.builder()
          .configuration(configuration)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(configuration)
          .dimension(embeddingModel.dimension())
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
          .documentSplitter(DocumentSplitters.recursive(segmentationParameters.getMaxSegmentSizeInChar(), segmentationParameters.getMaxOverlapSizeInChars()))
          .embeddingModel(embeddingModel)
          .embeddingStore(embeddingStore)
          .build();

      BaseStorage baseStorage = BaseStorage.builder()
          .storageConfiguration(storageConfiguration)
          .contextPath(documentParameters.getContextPath())
          .fileType(documentParameters.getFileType())
          .build();
      Document document = baseStorage.getSingleDocument();

      embeddingStoreIngestor.ingest(document);

      JSONObject jsonObject = JsonUtils.createIngestionStatusObject(storeName);

      return createEmbeddingResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
            put("storageType", baseStorage.getStorageType());
            put("fileType", documentParameters.getFileType());
            put("contextPath", documentParameters.getContextPath());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while adding document %s into the store %s", documentParameters.getContextPath(), storeName),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Query information from embedding store , provide the storeName (Index, Collections, etc.)
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("EMBEDDING-query-from-store")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingQueryResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      queryFromEmbedding( @Config Configuration configuration,
                          @Alias("storeName") @DisplayName("Store Name") String storeName,
                          String question,
                          Number maxResults,
                          Double minScore,
                          @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {
    try {

      int maximumResults = maxResults.intValue();
      if (minScore == null) { //|| minScore == 0) {
        minScore = Constants.EMBEDDING_SEARCH_REQUEST_DEFAULT_MIN_SCORE;
      }

      BaseModel baseModel = BaseModel.builder()
          .configuration(configuration)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(configuration)
          .dimension(embeddingModel.dimension())
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      Embedding questionEmbedding = embeddingModel.embed(question).content();

      EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
              .queryEmbedding(questionEmbedding)
              .maxResults(maximumResults)
              .minScore(minScore)
              .build();

      EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
      List<EmbeddingMatch<TextSegment>> embeddingMatches = searchResult.matches();

      String information = embeddingMatches.stream()
          .map(match -> match.embedded().text())
          .collect(joining("\n\n"));

      JSONObject jsonObject = new JSONObject();
      jsonObject.put(Constants.JSON_KEY_RESPONSE, information);
      jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);
      jsonObject.put(Constants.JSON_KEY_QUESTION, question);
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

      return createEmbeddingResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while querying embeddings with question %s from the store %s", question, storeName),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }



  /**
   * Query information from embedding store and filter results based on a metadata key filter
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("EMBEDDING-query-from-store-with-filter")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingQueryResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      queryByFilterFromEmbedding( String storeName,
                                  String question,
                                  Number maxResults,
                                  Double minScore,
                                  @Config Configuration configuration,
                                  @ParameterGroup(name = "Filter") MetadataFilterParameters.SearchFilterParameters searchFilterParams,
                                  @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {

      EmbeddingOperationValidator.validateOperationType(
              Constants.EMBEDDING_OPERATION_TYPE_FILTER_BY_METADATA,configuration.getStoreConfiguration().getVectorStore());

      int maximumResults = maxResults.intValue();
      if (minScore == null) { //|| minScore == 0) {
        minScore = Constants.EMBEDDING_SEARCH_REQUEST_DEFAULT_MIN_SCORE;
      }

      BaseModel baseModel = BaseModel.builder()
          .configuration(configuration)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(configuration)
          .dimension(embeddingModel.dimension())
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      Embedding questionEmbedding = embeddingModel.embed(question).content();

      EmbeddingSearchRequest.EmbeddingSearchRequestBuilder searchRequestBuilder = EmbeddingSearchRequest.builder()
              .queryEmbedding(questionEmbedding)
              .maxResults(maximumResults)
              .minScore(minScore);

      JSONObject jsonObject = new JSONObject();

      if(searchFilterParams.areFilterParamsSet()) {

        Filter filter = searchFilterParams.buildMetadataFilter();
        searchRequestBuilder.filter(filter);
        //jsonObject.put("filter", searchFilterParams.getFilterJSONObject());
      }

      EmbeddingSearchRequest searchRequest = searchRequestBuilder.build();

      EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
      List<EmbeddingMatch<TextSegment>> embeddingMatches = searchResult.matches();

      String information = embeddingMatches.stream()
              .map(match -> match.embedded().text())
              .collect(joining("\n\n"));

      jsonObject.put(Constants.JSON_KEY_RESPONSE, information);
      jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);
      jsonObject.put(Constants.JSON_KEY_QUESTION, question);
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


      return createEmbeddingResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
            put("filter", searchFilterParams.getFilterJSONObject());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while querying embeddings with question %s from the store %s", question, storeName),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }

  /**
   * Retrieves and lists sources from the specified embedding store.
   *
   * This method searches an embedding store for documents (sources) related to a simple query and collects metadata about
   * each matched document, such as file name, URL, and ingestion datetime. The results are returned as a JSON structure.
   *
   * @param storeName      the name of the embedding store to search
   * @param configuration  the configuration object providing access to connection details and other settings
   * @return an {@link InputStream} containing a JSON object with the store name and an array of source metadata.
   *
   * @MediaType(value = APPLICATION_JSON, strict = false)
   * @Alias("EMBEDDING-list-sources")
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("EMBEDDING-list-sources")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingListSourcesResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      listSourcesFromStore( String storeName,
                            @Config Configuration configuration,
                            @ParameterGroup(name = "Querying Strategy") QueryParameters queryParams
  ) {

    try {

      EmbeddingOperationValidator.validateOperationType(
          Constants.EMBEDDING_OPERATION_TYPE_QUERY_ALL,configuration.getStoreConfiguration().getVectorStore());
      EmbeddingOperationValidator.validateOperationType(
              Constants.EMBEDDING_OPERATION_TYPE_FILTER_BY_METADATA,configuration.getStoreConfiguration().getVectorStore());

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(configuration)
          .queryParams(queryParams)
          .build();

      JSONObject jsonObject = baseStore.listSources();

      return createEmbeddingResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while listing sources from the store %s", storeName),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }


  /**
   * Remove all documents based on a filter from a store
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  @Alias("EMBEDDING-remove-from-store-by-filter")
  @Throws(EmbeddingErrorTypeProvider.class)
  @OutputJsonType(schema = "api/response/EmbeddingRemoveFromStoreResponse.json")
  public org.mule.runtime.extension.api.runtime.operation.Result<InputStream, EmbeddingResponseAttributes>
      removeEmbeddingsByFilter( String storeName,
                                @Config Configuration configuration,
                                @ParameterGroup(name = "Filter") MetadataFilterParameters.RemoveFilterParameters removeFilterParams,
                                @ParameterGroup(name = "Embedding Model") EmbeddingModelParameters embeddingModelParameters) {

    try {
      EmbeddingOperationValidator.validateOperationType(
              Constants.EMBEDDING_OPERATION_TYPE_REMOVE_EMBEDDINGS,configuration.getStoreConfiguration().getVectorStore());
      EmbeddingOperationValidator.validateOperationType(
              Constants.EMBEDDING_OPERATION_TYPE_FILTER_BY_METADATA,configuration.getStoreConfiguration().getVectorStore());

      BaseModel baseModel = BaseModel.builder()
          .configuration(configuration)
          .embeddingModelParameters(embeddingModelParameters)
          .build();

      EmbeddingModel embeddingModel = baseModel.buildEmbeddingModel();

      BaseStore baseStore = BaseStore.builder()
          .storeName(storeName)
          .configuration(configuration)
          .dimension(embeddingModel.dimension())
          .build();

      EmbeddingStore<TextSegment> embeddingStore = baseStore.buildEmbeddingStore();

      Filter filter = removeFilterParams.buildMetadataFilter();

      embeddingStore.removeAll(filter);

      JSONObject jsonObject = new JSONObject();
      jsonObject.put(Constants.JSON_KEY_STATUS, Constants.OPERATION_STATUS_DELETED);

      return createEmbeddingResponse(
          jsonObject.toString(),
          new HashMap<String, Object>() {{
            put("storeName", storeName);
            put("filter", removeFilterParams.getFilterJSONObject());
          }});

    } catch (ModuleException me) {
      throw me;

    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error while removing embeddings from the store %s", storeName),
          MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE,
          e);
    }
  }
}
