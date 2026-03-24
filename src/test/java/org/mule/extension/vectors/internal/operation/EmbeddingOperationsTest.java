package org.mule.extension.vectors.internal.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.mule.extension.vectors.api.metadata.EmbeddingResponseAttributes;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingMediaBinaryParameters;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceFactoryBuilder;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class EmbeddingOperationsTest {

  private EmbeddingOperations embeddingOperations;
  private EmbeddingConfiguration configuration;
  private BaseModelConnection connection;
  private EmbeddingModelParameters modelParameters;
  private EmbeddingMediaBinaryParameters mediaParameters;

  private static final String MODEL_NAME = "test-model";
  private static final float[] VECTOR_3D = {0.1f, 0.2f, 0.3f};
  private static final float[] VECTOR_2D = {0.4f, 0.5f};

  @BeforeEach
  void setUp() {
    embeddingOperations = new EmbeddingOperations();
    configuration = mock(EmbeddingConfiguration.class);
    connection = mock(BaseModelConnection.class);
    modelParameters = mock(EmbeddingModelParameters.class);
    mediaParameters = mock(EmbeddingMediaBinaryParameters.class);
    when(modelParameters.getEmbeddingModelName()).thenReturn(MODEL_NAME);
  }

  @FunctionalInterface
  private interface ServiceConfigurer {

    void configure(EmbeddingService service) throws Exception;
  }

  private MockedConstruction<EmbeddingServiceFactoryBuilder> mockFactoryBuilder(ServiceConfigurer configurer) {
    return mockConstruction(EmbeddingServiceFactoryBuilder.class, (factoryMock, ctx) -> {
      EmbeddingServiceBuilder builder = mock(EmbeddingServiceBuilder.class);
      EmbeddingService service = mock(EmbeddingService.class);
      when(factoryMock.getBuilder(any(), any())).thenReturn(builder);
      when(builder.build()).thenReturn(service);
      configurer.configure(service);
    });
  }

  @SuppressWarnings("unchecked")
  private Response<List<Embedding>> textResponse(List<Embedding> embeddings, TokenUsage usage) {
    Response<List<Embedding>> response = mock(Response.class);
    when(response.content()).thenReturn(embeddings);
    when(response.tokenUsage()).thenReturn(usage);
    return response;
  }

  @SuppressWarnings("unchecked")
  private Response<Embedding> singleEmbeddingResponse(Embedding embedding, TokenUsage usage) {
    Response<Embedding> response = mock(Response.class);
    when(response.content()).thenReturn(embedding);
    when(response.tokenUsage()).thenReturn(usage);
    return response;
  }

  private JSONObject toJson(InputStream is) throws Exception {
    return new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
  }

  // ==================== generateEmbeddingFromText ====================

  @Test
  void generateEmbeddingFromText_singleInputWithTokenUsage() throws Exception {
    List<String> inputs = Collections.singletonList("hello world");
    TokenUsage usage = new TokenUsage(10, 5, 15);
    Response<List<Embedding>> response =
        textResponse(Collections.singletonList(new Embedding(VECTOR_3D)), usage);

    try (MockedConstruction<EmbeddingServiceFactoryBuilder> ignored = mockFactoryBuilder(
                                                                                         service -> when(service
                                                                                             .embedTexts(inputs))
                                                                                                 .thenReturn(response))) {

      Result<InputStream, EmbeddingResponseAttributes> result =
          embeddingOperations.generateEmbeddingFromText(configuration, connection, inputs, modelParameters);

      JSONObject json = toJson(result.getOutput());
      assertThat(json.getJSONArray("text-segments").length()).isEqualTo(1);
      assertThat(json.getJSONArray("text-segments").getJSONObject(0).getString("text"))
          .isEqualTo("hello world");
      assertThat(json.getJSONArray("text-segments").getJSONObject(0)
          .getJSONObject("metadata").getInt("index")).isZero();
      assertThat(json.getJSONArray("embeddings").length()).isEqualTo(1);
      assertThat(json.getInt("dimension")).isEqualTo(3);

      EmbeddingResponseAttributes attrs = result.getAttributes().get();
      assertThat(attrs.getEmbeddingModelName()).isEqualTo(MODEL_NAME);
      assertThat(attrs.getEmbeddingModelDimension()).isEqualTo(3);
      assertThat(attrs.getTokenUsage()).isNotNull();
      assertThat(attrs.getTokenUsage().getInputCount()).isEqualTo(10);
      assertThat(attrs.getTokenUsage().getOutputCount()).isEqualTo(5);
      assertThat(attrs.getTokenUsage().getTotalCount()).isEqualTo(15);
    }
  }

  @Test
  void generateEmbeddingFromText_multipleInputs() throws Exception {
    List<String> inputs = Arrays.asList("first", "second");
    List<Embedding> embeddings = Arrays.asList(new Embedding(VECTOR_3D), new Embedding(VECTOR_2D));
    Response<List<Embedding>> response = textResponse(embeddings, null);

    try (MockedConstruction<EmbeddingServiceFactoryBuilder> ignored = mockFactoryBuilder(
                                                                                         service -> when(service
                                                                                             .embedTexts(inputs))
                                                                                                 .thenReturn(response))) {

      Result<InputStream, EmbeddingResponseAttributes> result =
          embeddingOperations.generateEmbeddingFromText(configuration, connection, inputs, modelParameters);

      JSONObject json = toJson(result.getOutput());
      assertThat(json.getJSONArray("text-segments").length()).isEqualTo(2);
      assertThat(json.getJSONArray("text-segments").getJSONObject(0).getString("text"))
          .isEqualTo("first");
      assertThat(json.getJSONArray("text-segments").getJSONObject(1).getString("text"))
          .isEqualTo("second");
      assertThat(json.getJSONArray("text-segments").getJSONObject(1)
          .getJSONObject("metadata").getInt("index")).isEqualTo(1);
      assertThat(json.getJSONArray("embeddings").length()).isEqualTo(2);
      assertThat(json.getInt("dimension")).isEqualTo(VECTOR_3D.length);

      assertThat(result.getAttributes().get().getTokenUsage()).isNull();
    }
  }

  @Test
  void generateEmbeddingFromText_tokenUsageWithNullCounts() throws Exception {
    List<String> inputs = Collections.singletonList("test");
    TokenUsage usage = mock(TokenUsage.class);
    when(usage.inputTokenCount()).thenReturn(42);
    when(usage.outputTokenCount()).thenReturn(null);
    when(usage.totalTokenCount()).thenReturn(null);
    Response<List<Embedding>> response =
        textResponse(Collections.singletonList(new Embedding(VECTOR_3D)), usage);

    try (MockedConstruction<EmbeddingServiceFactoryBuilder> ignored = mockFactoryBuilder(
                                                                                         service -> when(service
                                                                                             .embedTexts(inputs))
                                                                                                 .thenReturn(response))) {

      Result<InputStream, EmbeddingResponseAttributes> result =
          embeddingOperations.generateEmbeddingFromText(configuration, connection, inputs, modelParameters);

      org.mule.extension.vectors.api.metadata.TokenUsage tokenUsage =
          result.getAttributes().get().getTokenUsage();
      assertThat(tokenUsage).isNotNull();
      assertThat(tokenUsage.getInputCount()).isEqualTo(42);
      assertThat(tokenUsage.getOutputCount()).isZero();
      assertThat(tokenUsage.getTotalCount()).isZero();
    }
  }

  @Test
  void generateEmbeddingFromText_noTokenUsage() throws Exception {
    List<String> inputs = Collections.singletonList("no-usage");
    Response<List<Embedding>> response =
        textResponse(Collections.singletonList(new Embedding(VECTOR_2D)), null);

    try (MockedConstruction<EmbeddingServiceFactoryBuilder> ignored = mockFactoryBuilder(
                                                                                         service -> when(service
                                                                                             .embedTexts(inputs))
                                                                                                 .thenReturn(response))) {

      Result<InputStream, EmbeddingResponseAttributes> result =
          embeddingOperations.generateEmbeddingFromText(configuration, connection, inputs, modelParameters);

      assertThat(result.getAttributes().get().getTokenUsage()).isNull();
      assertThat(toJson(result.getOutput()).getInt("dimension")).isEqualTo(2);
    }
  }

  @Test
  void generateEmbeddingFromText_exceptionWrapsInModuleException() {
    List<String> inputs = Collections.singletonList("fail");

    try (MockedConstruction<EmbeddingServiceFactoryBuilder> ignored = mockFactoryBuilder(
                                                                                         service -> when(service
                                                                                             .embedTexts(anyList()))
                                                                                                 .thenThrow(new RuntimeException("service failure")))) {

      assertThatThrownBy(() -> embeddingOperations.generateEmbeddingFromText(
                                                                             configuration, connection, inputs, modelParameters))
                                                                                 .isInstanceOf(ModuleException.class)
                                                                                 .hasMessageContaining("Error while generating embedding from texts")
                                                                                 .hasCauseInstanceOf(RuntimeException.class);
    }
  }

  @Test
  void buildTokenUsage_allNullCounts_returnsZeros() throws Exception {
    java.lang.reflect.Method m = EmbeddingOperations.class.getDeclaredMethod(
                                                                             "buildTokenUsage", Integer.class, Integer.class,
                                                                             Integer.class);
    m.setAccessible(true);
    org.mule.extension.vectors.api.metadata.TokenUsage usage =
        (org.mule.extension.vectors.api.metadata.TokenUsage) m.invoke(embeddingOperations, null, null, null);
    assertThat(usage.getInputCount()).isZero();
    assertThat(usage.getOutputCount()).isZero();
    assertThat(usage.getTotalCount()).isZero();
  }

  @Test
  void buildTokenUsage_allNonNullCounts_returnsValues() throws Exception {
    java.lang.reflect.Method m = EmbeddingOperations.class.getDeclaredMethod(
                                                                             "buildTokenUsage", Integer.class, Integer.class,
                                                                             Integer.class);
    m.setAccessible(true);
    org.mule.extension.vectors.api.metadata.TokenUsage usage =
        (org.mule.extension.vectors.api.metadata.TokenUsage) m.invoke(embeddingOperations, 10, 20, 30);
    assertThat(usage.getInputCount()).isEqualTo(10);
    assertThat(usage.getOutputCount()).isEqualTo(20);
    assertThat(usage.getTotalCount()).isEqualTo(30);
  }

  @Test
  void buildTokenUsage_mixedNullCounts_handlesCorrectly() throws Exception {
    java.lang.reflect.Method m = EmbeddingOperations.class.getDeclaredMethod(
                                                                             "buildTokenUsage", Integer.class, Integer.class,
                                                                             Integer.class);
    m.setAccessible(true);
    org.mule.extension.vectors.api.metadata.TokenUsage usage =
        (org.mule.extension.vectors.api.metadata.TokenUsage) m.invoke(embeddingOperations, 5, null, 10);
    assertThat(usage.getInputCount()).isEqualTo(5);
    assertThat(usage.getOutputCount()).isZero();
    assertThat(usage.getTotalCount()).isEqualTo(10);
  }
}
