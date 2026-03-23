package org.mule.extension.vectors.internal.service.embeddings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceFactoryBuilderTest {

  static Stream<Arguments> serviceTypeProvider() {
    return Stream.of(
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION, AzureAIVisionModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI, AzureOpenAIModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN, EinsteinModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE, HuggingFaceModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI, MistralAIModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_NOMIC, NomicModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_OLLAMA, OllamaModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_OPENAI, OpenAIModelConnection.class),
                     Arguments.of(Constants.EMBEDDING_MODEL_SERVICE_VERTEX_AI, VertexAIModelConnection.class));
  }

  @ParameterizedTest
  @MethodSource("serviceTypeProvider")
  void getBuilder_returnsCorrectBuilder(String serviceType, Class<? extends BaseModelConnection> connectionClass) {
    BaseModelConnection conn = (BaseModelConnection) mock(connectionClass, withSettings().lenient());
    lenient().when(conn.getEmbeddingModelService()).thenReturn(serviceType);
    EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);

    EmbeddingServiceFactoryBuilder factory = new EmbeddingServiceFactoryBuilder(conn);
    EmbeddingServiceBuilder result = factory.getBuilder(conn, params);

    assertThat(result).isNotNull();
  }

  @Test
  void getBuilder_unsupportedService_throwsIllegalArgumentException() {
    BaseModelConnection conn = mock(BaseModelConnection.class);
    lenient().when(conn.getEmbeddingModelService()).thenReturn("UNSUPPORTED");
    EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);

    EmbeddingServiceFactoryBuilder factory = new EmbeddingServiceFactoryBuilder(conn);

    assertThatThrownBy(() -> factory.getBuilder(conn, params))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported");
  }
}
