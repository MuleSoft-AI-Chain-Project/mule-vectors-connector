package org.mule.extension.vectors.internal.helper;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.CustomMetadata;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.store.StoreOperationsHelper;
import org.mule.extension.vectors.internal.service.store.VectorStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

class StoreOperationsHelperTest {

    @Test
    void parseStoreInput_validAddOperation_withCustomMetadata() throws Exception {
        JSONObject json = new JSONObject();
        JSONArray textSegments = new JSONArray();
        JSONObject segment = new JSONObject();
        segment.put(Constants.JSON_KEY_TEXT, "hello");
        segment.put(Constants.JSON_KEY_METADATA, new JSONObject().put("foo", "bar"));
        textSegments.put(segment);
        json.put(Constants.JSON_KEY_TEXT_SEGMENTS, textSegments);
        JSONArray embeddings = new JSONArray();
        embeddings.put(new JSONArray().put(1.0).put(2.0));
        json.put(Constants.JSON_KEY_EMBEDDINGS, embeddings);
        json.put(Constants.JSON_KEY_DIMENSION, 2);
        InputStream is = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
        CustomMetadata customMetadata = new CustomMetadata();
        Field entriesField = CustomMetadata.class.getDeclaredField("metadataEntries");
        entriesField.setAccessible(true);
        entriesField.set(customMetadata, new java.util.HashMap<>(Map.of("baz", "qux")));
        var result = StoreOperationsHelper.parseStoreInput(is, true, customMetadata);
        assertThat(result.textSegments()).hasSize(1);
        assertThat(result.textSegments().get(0).text()).isEqualTo("hello");
        assertThat(result.textSegments().get(0).metadata().containsKey("foo")).isTrue();
        assertThat(result.textSegments().get(0).metadata().containsKey("baz")).isTrue();
        assertThat(result.embeddings()).hasSize(1);
        assertThat(result.embeddings().get(0).vector()).containsExactly(1.0f, 2.0f);
        assertThat(result.dimension()).isEqualTo(2);
    }

    @Test
    void parseStoreInput_validNonAddOperation_singleSegmentAndEmbedding() throws Exception {
        JSONObject json = new JSONObject();
        JSONArray textSegments = new JSONArray();
        JSONObject segment = new JSONObject();
        segment.put(Constants.JSON_KEY_TEXT, "hi");
        segment.put(Constants.JSON_KEY_METADATA, new JSONObject().put("foo", "bar"));
        textSegments.put(segment);
        json.put(Constants.JSON_KEY_TEXT_SEGMENTS, textSegments);
        JSONArray embeddings = new JSONArray();
        embeddings.put(new JSONArray().put(3.0).put(4.0));
        json.put(Constants.JSON_KEY_EMBEDDINGS, embeddings);
        json.put(Constants.JSON_KEY_DIMENSION, 2);
        InputStream is = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
        var result = StoreOperationsHelper.parseStoreInput(is, false, null);
        assertThat(result.textSegments()).hasSize(1);
        assertThat(result.embeddings()).hasSize(1);
        assertThat(result.dimension()).isEqualTo(2);
    }

    @Test
    void parseStoreInput_nonAddOperation_multipleSegments_throws() {
        JSONObject json = new JSONObject();
        JSONArray textSegments = new JSONArray();
        textSegments.put(new JSONObject().put(Constants.JSON_KEY_TEXT, "a").put(Constants.JSON_KEY_METADATA, new JSONObject()));
        textSegments.put(new JSONObject().put(Constants.JSON_KEY_TEXT, "b").put(Constants.JSON_KEY_METADATA, new JSONObject()));
        json.put(Constants.JSON_KEY_TEXT_SEGMENTS, textSegments);
        JSONArray embeddings = new JSONArray();
        embeddings.put(new JSONArray().put(1.0));
        json.put(Constants.JSON_KEY_EMBEDDINGS, embeddings);
        json.put(Constants.JSON_KEY_DIMENSION, 1);
        InputStream is = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> StoreOperationsHelper.parseStoreInput(is, false, null))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("one text segment only");
    }

    @Test
    void parseStoreInput_nonAddOperation_multipleEmbeddings_throws() {
        JSONObject json = new JSONObject();
        JSONArray textSegments = new JSONArray();
        textSegments.put(new JSONObject().put(Constants.JSON_KEY_TEXT, "a").put(Constants.JSON_KEY_METADATA, new JSONObject()));
        json.put(Constants.JSON_KEY_TEXT_SEGMENTS, textSegments);
        JSONArray embeddings = new JSONArray();
        embeddings.put(new JSONArray().put(1.0));
        embeddings.put(new JSONArray().put(2.0));
        json.put(Constants.JSON_KEY_EMBEDDINGS, embeddings);
        json.put(Constants.JSON_KEY_DIMENSION, 1);
        InputStream is = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> StoreOperationsHelper.parseStoreInput(is, false, null))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("one embedding only");
    }

    @Test
    void parseStoreInput_invalidDimension_throws() {
        JSONObject json = new JSONObject();
        JSONArray textSegments = new JSONArray();
        textSegments.put(new JSONObject().put(Constants.JSON_KEY_TEXT, "a").put(Constants.JSON_KEY_METADATA, new JSONObject()));
        json.put(Constants.JSON_KEY_TEXT_SEGMENTS, textSegments);
        JSONArray embeddings = new JSONArray();
        embeddings.put(new JSONArray().put(1.0));
        json.put(Constants.JSON_KEY_EMBEDDINGS, embeddings);
        json.put(Constants.JSON_KEY_DIMENSION, 0);
        InputStream is = new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
        Throwable thrown = catchThrowable(() -> StoreOperationsHelper.parseStoreInput(is, true, null));
        assertThat(thrown).isInstanceOf(ModuleException.class);
        assertThat(thrown.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown.getCause().getMessage()).isEqualTo("dimension must be greater than zero, but is: 0");
    }

    @Test
    void parseStoreInput_invalidJson_throws() {
        InputStream is = new ByteArrayInputStream("not a json".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> StoreOperationsHelper.parseStoreInput(is, true, null))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("parsing Text Segments");
    }

    @Test
    void executeStoreOperation_happyPath() throws Exception {
        StoreConfiguration config = new StoreConfiguration();
        BaseStoreConnection conn = new BaseStoreConnection() {
            @Override public String getVectorStore() { return "store"; }
            @Override public BaseStoreConnectionParameters getConnectionParameters() { return null; }
            @Override public void disconnect() {}
            @Override public void validate() {}
        };
        String storeName = "store";
        int dimension = 2;
        boolean createStore = false;
        QueryParameters queryParams = new QueryParameters();
        Field pageSizeField = QueryParameters.class.getDeclaredField("pageSize");
        pageSizeField.setAccessible(true);
        pageSizeField.set(queryParams, 2);
        Field retrieveEmbeddingsField = QueryParameters.class.getDeclaredField("retrieveEmbeddings");
        retrieveEmbeddingsField.setAccessible(true);
        retrieveEmbeddingsField.set(queryParams, true);
        Function<VectorStoreService, String> operation = svc -> "result";
        Function<String, JSONObject> responseBuilder = str -> new JSONObject().put("foo", str);
        HashMap<String, Object> attributes = new HashMap<>();
        StoreOperationsHelper.StoreOperationContext context = new StoreOperationsHelper.StoreOperationContext(
            config, conn, storeName, dimension, createStore, queryParams, attributes);
        assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(context, operation, responseBuilder))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Error while initializing vector store service. \"store\" not supported.");
    }

    @Test
    void executeStoreOperation_moduleException() throws Exception {
        StoreConfiguration config = new StoreConfiguration();
        BaseStoreConnection conn = new BaseStoreConnection() {
            @Override public String getVectorStore() { return "store"; }
            @Override public BaseStoreConnectionParameters getConnectionParameters() { return null; }
            @Override public void disconnect() {}
            @Override public void validate() {}
        };
        String storeName = "store";
        int dimension = 2;
        boolean createStore = false;
        QueryParameters queryParams = new QueryParameters();
        Field pageSizeField = QueryParameters.class.getDeclaredField("pageSize");
        pageSizeField.setAccessible(true);
        pageSizeField.set(queryParams, 2);
        Field retrieveEmbeddingsField = QueryParameters.class.getDeclaredField("retrieveEmbeddings");
        retrieveEmbeddingsField.setAccessible(true);
        retrieveEmbeddingsField.set(queryParams, true);
        Function<VectorStoreService, String> operation = svc -> { throw new ModuleException("fail", MuleVectorsErrorType.INVALID_PARAMETER); };
        Function<String, JSONObject> responseBuilder = str -> new JSONObject().put("foo", str);
        HashMap<String, Object> attributes = new HashMap<>();
        StoreOperationsHelper.StoreOperationContext context = new StoreOperationsHelper.StoreOperationContext(
            config, conn, storeName, dimension, createStore, queryParams, attributes);
        assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(context, operation, responseBuilder))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Error while initializing vector store service. \"store\" not supported.");
    }

    @Test
    void executeStoreOperation_unsupportedOperationException() throws Exception {
        StoreConfiguration config = new StoreConfiguration();
        BaseStoreConnection conn = new BaseStoreConnection() {
            @Override public String getVectorStore() { return "store"; }
            @Override public BaseStoreConnectionParameters getConnectionParameters() { return null; }
            @Override public void disconnect() {}
            @Override public void validate() {}
        };
        String storeName = "store";
        int dimension = 2;
        boolean createStore = false;
        QueryParameters queryParams = new QueryParameters();
        Field pageSizeField = QueryParameters.class.getDeclaredField("pageSize");
        pageSizeField.setAccessible(true);
        pageSizeField.set(queryParams, 2);
        Field retrieveEmbeddingsField = QueryParameters.class.getDeclaredField("retrieveEmbeddings");
        retrieveEmbeddingsField.setAccessible(true);
        retrieveEmbeddingsField.set(queryParams, true);
        Function<VectorStoreService, String> operation = svc -> { throw new UnsupportedOperationException("not supported"); };
        Function<String, JSONObject> responseBuilder = str -> new JSONObject().put("foo", str);
        HashMap<String, Object> attributes = new HashMap<>();
        StoreOperationsHelper.StoreOperationContext context = new StoreOperationsHelper.StoreOperationContext(
            config, conn, storeName, dimension, createStore, queryParams, attributes);
        assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(context, operation, responseBuilder))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Error while initializing vector store service. \"store\" not supported.");
    }

    @Test
    void executeStoreOperation_genericException() throws Exception {
        StoreConfiguration config = new StoreConfiguration();
        BaseStoreConnection conn = new BaseStoreConnection() {
            @Override public String getVectorStore() { return "store"; }
            @Override public BaseStoreConnectionParameters getConnectionParameters() { return null; }
            @Override public void disconnect() {}
            @Override public void validate() {}
        };
        String storeName = "store";
        int dimension = 2;
        boolean createStore = false;
        QueryParameters queryParams = new QueryParameters();
        Field pageSizeField = QueryParameters.class.getDeclaredField("pageSize");
        pageSizeField.setAccessible(true);
        pageSizeField.set(queryParams, 2);
        Field retrieveEmbeddingsField = QueryParameters.class.getDeclaredField("retrieveEmbeddings");
        retrieveEmbeddingsField.setAccessible(true);
        retrieveEmbeddingsField.set(queryParams, true);
        Function<VectorStoreService, String> operation = svc -> { throw new RuntimeException("boom"); };
        Function<String, JSONObject> responseBuilder = str -> new JSONObject().put("foo", str);
        HashMap<String, Object> attributes = new HashMap<>();
        StoreOperationsHelper.StoreOperationContext context = new StoreOperationsHelper.StoreOperationContext(
            config, conn, storeName, dimension, createStore, queryParams, attributes);
        assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(context, operation, responseBuilder))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Error while initializing vector store service. \"store\" not supported.");
    }

    @Test
    void getMetadataMap_allFields() {
        File file = new File(new ByteArrayInputStream(new byte[]{1}), "/path", "file.txt", "text/plain", Map.of("foo", "bar"));
        Map<String, Object> map = StoreOperationsHelper.getMetadataMap(file);
        assertThat(map.get("path")).isEqualTo("/path");
        assertThat(map.get("fileName")).isEqualTo("file.txt");
        assertThat(map.get("mimeType")).isEqualTo("text/plain");
        assertThat(((Map<?,?>)map.get("metadata")).get("foo")).isEqualTo("bar");
    }

    @Test
    void getMetadataMap_nullFields() {
        File file = new File(null, null, null, (String) null, null);
        Map<String, Object> map = StoreOperationsHelper.getMetadataMap(file);
        assertThat(map.get("path")).isNull();
        assertThat(map.get("fileName")).isNull();
        assertThat(map.get("mimeType")).isNull();
        assertThat(map.get("metadata")).isNull();
    }
} 
