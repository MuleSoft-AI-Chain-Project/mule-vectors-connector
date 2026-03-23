package org.mule.extension.vectors.internal.helper.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.CustomMetadata;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.store.StoreOperationsHelper.ParsedStoreInput;
import org.mule.extension.vectors.internal.helper.store.StoreOperationsHelper.StoreOperationContext;
import org.mule.extension.vectors.internal.service.store.VectorStoreService;
import org.mule.extension.vectors.internal.service.store.VectorStoreServiceProviderFactory;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class StoreOperationsHelperTest {

  private InputStream toStream(String json) {
    return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void parseStoreInput_success() {
    String json = "{\"text-segments\":[{\"text\":\"hello\",\"metadata\":{}}]," +
        "\"embeddings\":[[0.1,0.2]],\"dimension\":2}";

    try (MockedStatic<MetadataUtils> utils = mockStatic(MetadataUtils.class)) {
      utils.when(MetadataUtils::getIngestionMetadata).thenReturn(new HashMap<>());

      ParsedStoreInput result = StoreOperationsHelper.parseStoreInput(toStream(json), true, null);

      assertThat(result.textSegments()).hasSize(1);
      assertThat(result.textSegments().get(0).text()).isEqualTo("hello");
      assertThat(result.embeddings()).hasSize(1);
      assertThat(result.embeddings().get(0).vector()).hasSize(2);
      assertThat(result.dimension()).isEqualTo(2);
    }
  }

  @Test
  void parseStoreInput_queryMode_throwsOnMultipleSegments() {
    String json = "{\"text-segments\":[" +
        "{\"text\":\"a\",\"metadata\":{}}," +
        "{\"text\":\"b\",\"metadata\":{}}" +
        "],\"embeddings\":[[0.1]],\"dimension\":1}";

    assertThatThrownBy(() -> StoreOperationsHelper.parseStoreInput(toStream(json), false, null))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("one text segment only");
  }

  @Test
  void parseEmbeddings_queryMode_throwsOnMultipleEmbeddings() {
    String json = "{\"text-segments\":[{\"text\":\"a\",\"metadata\":{}}]," +
        "\"embeddings\":[[0.1],[0.2]],\"dimension\":1}";

    assertThatThrownBy(() -> StoreOperationsHelper.parseStoreInput(toStream(json), false, null))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("one embedding only");
  }

  @Test
  void parseStoreInput_invalidJson_throwsModuleException() {
    assertThatThrownBy(() -> StoreOperationsHelper.parseStoreInput(toStream("not-json"), true, null))
        .isInstanceOf(ModuleException.class);
  }

  @Test
  void getMetadataMap_extractsFileInfoFields() {
    Map<String, Object> meta = Map.of("key", "value");
    FileInfo file = new FileInfo(null, "/tmp/test", "test.txt", "text/plain", meta);

    Map<String, Object> result = StoreOperationsHelper.getMetadataMap(file);

    assertThat(result)
        .containsEntry("path", "/tmp/test")
        .containsEntry("fileName", "test.txt")
        .containsEntry("mimeType", "text/plain")
        .containsEntry("metadata", meta);
  }

  @Test
  void storeOperationContext_getters() {
    StoreConfiguration config = mock(StoreConfiguration.class);
    BaseStoreConnection connection = mock(BaseStoreConnection.class);
    QueryParameters queryParams = mock(QueryParameters.class);
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("k", "v");

    StoreOperationContext ctx = new StoreOperationContext(
                                                          config, connection, "myStore", 128, true, queryParams, attributes);

    assertThat(ctx.getStoreConfiguration()).isSameAs(config);
    assertThat(ctx.getStoreConnection()).isSameAs(connection);
    assertThat(ctx.getStoreName()).isEqualTo("myStore");
    assertThat(ctx.getDimension()).isEqualTo(128);
    assertThat(ctx.isCreateStore()).isTrue();
    assertThat(ctx.getQueryParams()).isSameAs(queryParams);
    assertThat(ctx.getAttributes()).isSameAs(attributes);
  }

  @Test
  void parsedStoreInput_record() {
    ParsedStoreInput input = new ParsedStoreInput(
                                                  java.util.List.of(), java.util.List.of(), 5, new HashMap<>());

    assertThat(input.textSegments()).isEmpty();
    assertThat(input.embeddings()).isEmpty();
    assertThat(input.dimension()).isEqualTo(5);
    assertThat(input.ingestionMetadata()).isEmpty();
  }

  @Test
  void executeStoreOperation_success() {
    StoreConfiguration config = mock(StoreConfiguration.class);
    BaseStoreConnection connection = mock(BaseStoreConnection.class);
    QueryParameters queryParams = mock(QueryParameters.class);
    VectorStoreService service = mock(VectorStoreService.class);
    HashMap<String, Object> attributes = new HashMap<>();

    StoreOperationContext ctx = new StoreOperationContext(
                                                          config, connection, "store", 64, false, queryParams, attributes);

    Function<VectorStoreService, String> operation = svc -> "result";
    Function<String, JSONObject> responseBuilder = res -> new JSONObject().put("status", "ok");

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(
                                                                      config, connection, "store", queryParams, 64, false))
          .thenReturn(service);

      var result = StoreOperationsHelper.executeStoreOperation(ctx, operation, responseBuilder);

      assertThat(result).isNotNull();
      assertThat(result.getOutput()).isNotNull();
    }
  }

  @Test
  void executeStoreOperation_moduleExceptionPassthrough() {
    StoreConfiguration config = mock(StoreConfiguration.class);
    BaseStoreConnection connection = mock(BaseStoreConnection.class);
    QueryParameters queryParams = mock(QueryParameters.class);
    VectorStoreService service = mock(VectorStoreService.class);
    HashMap<String, Object> attributes = new HashMap<>();

    StoreOperationContext ctx = new StoreOperationContext(
                                                          config, connection, "store", 64, false, queryParams, attributes);

    ModuleException expected = new ModuleException("test", MuleVectorsErrorType.INVALID_PARAMETER);
    Function<VectorStoreService, String> operation = svc -> {
      throw expected;
    };
    Function<String, JSONObject> responseBuilder = res -> new JSONObject();

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(
                                                                      config, connection, "store", queryParams, 64, false))
          .thenReturn(service);

      assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(ctx, operation, responseBuilder))
          .isSameAs(expected);
    }
  }

  @Test
  void executeStoreOperation_unsupportedOperation() {
    StoreConfiguration config = mock(StoreConfiguration.class);
    BaseStoreConnection connection = mock(BaseStoreConnection.class);
    QueryParameters queryParams = mock(QueryParameters.class);
    VectorStoreService service = mock(VectorStoreService.class);
    HashMap<String, Object> attributes = new HashMap<>();

    StoreOperationContext ctx = new StoreOperationContext(
                                                          config, connection, "store", 64, false, queryParams, attributes);

    Function<VectorStoreService, String> operation = svc -> {
      throw new UnsupportedOperationException("nope");
    };
    Function<String, JSONObject> responseBuilder = res -> new JSONObject();

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(
                                                                      config, connection, "store", queryParams, 64, false))
          .thenReturn(service);

      assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(ctx, operation, responseBuilder))
          .isInstanceOf(ModuleException.class)
          .satisfies(ex -> assertThat(((ModuleException) ex).getType())
              .isEqualTo(MuleVectorsErrorType.STORE_UNSUPPORTED_OPERATION));
    }
  }

  @Test
  void executeStoreOperation_interruptedException() {
    StoreConfiguration config = mock(StoreConfiguration.class);
    BaseStoreConnection connection = mock(BaseStoreConnection.class);
    QueryParameters queryParams = mock(QueryParameters.class);
    HashMap<String, Object> attributes = new HashMap<>();

    StoreOperationContext ctx = new StoreOperationContext(
                                                          config, connection, "store", 64, false, queryParams, attributes);

    Function<VectorStoreService, String> operation = svc -> "unused";
    Function<String, JSONObject> responseBuilder = res -> new JSONObject();

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(
                                                                      config, connection, "store", queryParams, 64, false))
          .thenThrow(new InterruptedException("interrupted"));

      assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(ctx, operation, responseBuilder))
          .isInstanceOf(ModuleException.class)
          .satisfies(ex -> assertThat(((ModuleException) ex).getType())
              .isEqualTo(MuleVectorsErrorType.STORE_OPERATIONS_FAILURE))
          .hasCauseInstanceOf(InterruptedException.class);
    }
  }

  @Test
  void executeStoreOperation_generalException() {
    StoreConfiguration config = mock(StoreConfiguration.class);
    BaseStoreConnection connection = mock(BaseStoreConnection.class);
    QueryParameters queryParams = mock(QueryParameters.class);
    VectorStoreService service = mock(VectorStoreService.class);
    HashMap<String, Object> attributes = new HashMap<>();

    StoreOperationContext ctx = new StoreOperationContext(
                                                          config, connection, "store", 64, false, queryParams, attributes);

    Function<VectorStoreService, String> operation = svc -> {
      throw new RuntimeException("boom");
    };
    Function<String, JSONObject> responseBuilder = res -> new JSONObject();

    try (MockedStatic<VectorStoreServiceProviderFactory> factory =
        mockStatic(VectorStoreServiceProviderFactory.class)) {
      factory.when(() -> VectorStoreServiceProviderFactory.getService(
                                                                      config, connection, "store", queryParams, 64, false))
          .thenReturn(service);

      assertThatThrownBy(() -> StoreOperationsHelper.executeStoreOperation(ctx, operation, responseBuilder))
          .isInstanceOf(ModuleException.class)
          .satisfies(ex -> assertThat(((ModuleException) ex).getType())
              .isEqualTo(MuleVectorsErrorType.STORE_OPERATIONS_FAILURE))
          .hasCauseInstanceOf(RuntimeException.class);
    }
  }

  @Test
  void parseTextSegments_withCustomMetadata() {
    String json = "{\"text-segments\":[{\"text\":\"hello\",\"metadata\":{\"existing\":\"val\"}}]," +
        "\"embeddings\":[[0.5]],\"dimension\":1}";

    CustomMetadata customMetadata = mock(CustomMetadata.class);
    HashMap<String, String> entries = new HashMap<>();
    entries.put("custom_key", "custom_value");
    when(customMetadata.getMetadataEntries()).thenReturn(entries);

    try (MockedStatic<MetadataUtils> utils = mockStatic(MetadataUtils.class)) {
      utils.when(MetadataUtils::getIngestionMetadata).thenReturn(new HashMap<>());

      ParsedStoreInput result = StoreOperationsHelper.parseStoreInput(
                                                                      toStream(json), true, customMetadata);

      assertThat(result.textSegments()).hasSize(1);
      assertThat(result.textSegments().get(0).metadata().getString("custom_key"))
          .isEqualTo("custom_value");
      assertThat(result.textSegments().get(0).metadata().getString("existing"))
          .isEqualTo("val");
    }
  }
}
