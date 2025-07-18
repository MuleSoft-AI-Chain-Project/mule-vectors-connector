package org.mule.extension.vectors.internal.store.pinecone;

import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.pinecone.PineconeStore;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PineconeStoreTest {
    @Mock StoreConfiguration storeConfiguration;
    @Mock PineconeStoreConnection pineconeStoreConnection;
    @Mock QueryParameters queryParameters;

    @BeforeEach
    void setup() {
        when(pineconeStoreConnection.getApiKey()).thenReturn("api-key");
        when(pineconeStoreConnection.getCloud()).thenReturn("cloud");
        when(pineconeStoreConnection.getRegion()).thenReturn("region");
    }

    @Test
    void constructor_assignsFields_andValidatesPageSize() {
        when(queryParameters.pageSize()).thenReturn(10);
        PineconeStore store = new PineconeStore(storeConfiguration, pineconeStoreConnection, "store", queryParameters, 128, true);
        assertThat(store).extracting("apiKey", "cloud", "region", "queryParams")
                .containsExactly("api-key", "cloud", "region", queryParameters);
        verify(queryParameters).pageSize();
    }

    @Test
    void buildEmbeddingStore_createStoreTrue_callsBuilderWithCreateIndex() {
        try (MockedStatic<PineconeEmbeddingStore> pineconeStatic = mockStatic(PineconeEmbeddingStore.class)) {
            PineconeEmbeddingStore.Builder builder = mock(PineconeEmbeddingStore.Builder.class, RETURNS_SELF);
            PineconeEmbeddingStore.Builder builderWithIndex = mock(PineconeEmbeddingStore.Builder.class, RETURNS_SELF);
            PineconeEmbeddingStore storeMock = mock(PineconeEmbeddingStore.class);
            pineconeStatic.when(PineconeEmbeddingStore::builder).thenReturn(builder);
            when(builder.apiKey(anyString())).thenReturn(builder);
            when(builder.index(anyString())).thenReturn(builder);
            when(builder.nameSpace(anyString())).thenReturn(builder);
            when(builder.createIndex(any())).thenReturn(builderWithIndex);
            when(builderWithIndex.build()).thenReturn(storeMock);

            PineconeStore store = new PineconeStore(storeConfiguration, pineconeStoreConnection, "store", queryParameters, 128, true);
            Object result = store.buildEmbeddingStore();
            assertThat(result).isSameAs(storeMock);
            verify(builder).apiKey("api-key");
            verify(builder).index("store");
            verify(builder).nameSpace("ns0mc_store");
            ArgumentCaptor<PineconeServerlessIndexConfig> captor = ArgumentCaptor.forClass(PineconeServerlessIndexConfig.class);
            verify(builder).createIndex(captor.capture());
            PineconeServerlessIndexConfig config = captor.getValue();
            assertThat(config).isNotNull();
            // Use reflection to check fields since getters may not exist
            try {
                java.lang.reflect.Field cloudField = config.getClass().getDeclaredField("cloud");
                java.lang.reflect.Field regionField = config.getClass().getDeclaredField("region");
                java.lang.reflect.Field dimensionField = config.getClass().getDeclaredField("dimension");
                cloudField.setAccessible(true);
                regionField.setAccessible(true);
                dimensionField.setAccessible(true);
                assertThat(cloudField.get(config)).isEqualTo("cloud");
                assertThat(regionField.get(config)).isEqualTo("region");
                assertThat(dimensionField.get(config)).isEqualTo(128);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Reflection failed: " + e.getMessage());
            }
            verify(builderWithIndex).build();
        }
    }

    @Test
    void buildEmbeddingStore_createStoreFalse_callsBuilderWithoutCreateIndex() {
        try (MockedStatic<PineconeEmbeddingStore> pineconeStatic = mockStatic(PineconeEmbeddingStore.class)) {
            PineconeEmbeddingStore.Builder builder = mock(PineconeEmbeddingStore.Builder.class, RETURNS_SELF);
            PineconeEmbeddingStore storeMock = mock(PineconeEmbeddingStore.class);
            pineconeStatic.when(PineconeEmbeddingStore::builder).thenReturn(builder);
            when(builder.apiKey(anyString())).thenReturn(builder);
            when(builder.index(anyString())).thenReturn(builder);
            when(builder.nameSpace(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(storeMock);

            PineconeStore store = new PineconeStore(storeConfiguration, pineconeStoreConnection, "store", queryParameters, 128, false);
            Object result = store.buildEmbeddingStore();
            assertThat(result).isSameAs(storeMock);
            verify(builder).apiKey("api-key");
            verify(builder).index("store");
            verify(builder).nameSpace("ns0mc_store");
            verify(builder).build();
            verify(builder, never()).createIndex(any());
        }
    }

    @Test
    void buildEmbeddingStore_builderThrows_propagatesException() {
        try (MockedStatic<PineconeEmbeddingStore> pineconeStatic = mockStatic(PineconeEmbeddingStore.class)) {
            PineconeEmbeddingStore.Builder builder = mock(PineconeEmbeddingStore.Builder.class, RETURNS_SELF);
            pineconeStatic.when(PineconeEmbeddingStore::builder).thenReturn(builder);
            when(builder.apiKey(anyString())).thenReturn(builder);
            when(builder.index(anyString())).thenReturn(builder);
            when(builder.nameSpace(anyString())).thenReturn(builder);
            when(builder.build()).thenThrow(new RuntimeException("fail"));

            PineconeStore store = new PineconeStore(storeConfiguration, pineconeStoreConnection, "store", queryParameters, 128, false);
            assertThatThrownBy(store::buildEmbeddingStore)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("fail");
        }
    }
}
