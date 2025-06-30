package org.mule.extension.vectors.internal.store.ephemeralfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure JUnit 5 test for EphemeralFileStoreServiceProvider.
 *
 * Note: EphemeralFileStoreTest is deleted/skipped because pure unit tests are not feasible due to the
 * inability to mock EphemeralFileEmbeddingStore construction (see MDC and project policy).
 *
 * getFileIterator() is not unit tested, as it requires file IO and non-mockable types. This is documented
 * and skipped per project policy.
 */
@ExtendWith(MockitoExtension.class)
class EphemeralFileStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private EphemeralFileStoreConnection connection;
    @Mock
    private QueryParameters queryParameters;
    private static final String STORE_NAME = "test-store";
    private static final int DIMENSION = 1536;
    private static final boolean CREATE_STORE = true;
    private EphemeralFileStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new EphemeralFileStoreServiceProvider(storeConfiguration, connection, STORE_NAME, queryParameters, DIMENSION, CREATE_STORE);
    }

    @Test
    void testGetServiceReturnsEphemeralFileStore() {
        VectorStoreService service = provider.getService();
        assertNotNull(service);
        assertTrue(service instanceof EphemeralFileStore);
    }
} 