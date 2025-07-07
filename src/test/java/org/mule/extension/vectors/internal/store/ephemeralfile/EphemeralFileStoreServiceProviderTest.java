package org.mule.extension.vectors.internal.store.ephemeralfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EphemeralFileStoreServiceProviderTest {
    StoreConfiguration config;
    EphemeralFileStoreConnection connection;
    QueryParameters queryParams;

    @BeforeEach
    void setup() {
        config = mock(StoreConfiguration.class);
        connection = mock(EphemeralFileStoreConnection.class);
        queryParams = mock(QueryParameters.class);
    }

    @Test
    void constructor_assigns_fields() {
        EphemeralFileStoreServiceProvider provider = new EphemeralFileStoreServiceProvider(config, connection, "store", queryParams, 42, true);
        assertThat(provider).isNotNull();
    }

    @Test
    void getService_returns_EphemeralFileStore() {
        EphemeralFileStoreServiceProvider provider = new EphemeralFileStoreServiceProvider(config, connection, "store", queryParams, 42, true);
        var service = provider.getService();
        assertThat(service).isInstanceOf(EphemeralFileStore.class);
    }

    @Test
    void getFileIterator_returns_EphemeralFileStoreIterator() {
        try (MockedConstruction<EphemeralFileEmbeddingStore> construction =
                Mockito.mockConstruction(EphemeralFileEmbeddingStore.class,
                        (mock, context) -> when(mock.serializeToJson()).thenReturn("{\"entries\":[]}"))) {
            EphemeralFileStoreServiceProvider provider = new EphemeralFileStoreServiceProvider(config, connection, "store", queryParams, 42, true);
            var iterator = provider.getFileIterator();
            assertThat(iterator).isInstanceOf(EphemeralFileStoreIterator.class);
        }
    }
} 