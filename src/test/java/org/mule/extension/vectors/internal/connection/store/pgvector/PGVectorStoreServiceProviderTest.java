package org.mule.extension.vectors.internal.connection.store.pgvector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStore;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStoreIterator;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStoreServiceProvider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
class PGVectorStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private PGVectorStoreConnection pgVectorStoreConnection;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement pstmt;
    @Mock
    private ResultSet resultSet;

    private static final String STORE_NAME = "test-pgvector";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private PGVectorStoreServiceProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(pgVectorStoreConnection.getDataSource()).thenReturn(dataSource);
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        lenient().when(pstmt.executeQuery()).thenReturn(resultSet);
        provider = new PGVectorStoreServiceProvider(
                storeConfiguration,
                pgVectorStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void getService_returnsPGVectorStoreWithCorrectParams() {
        var service = provider.getService();
        assertThat(service).isInstanceOf(PGVectorStore.class);
        // Optionally, use reflection to check fields if needed
    }

    @Test
    void getFileIterator_returnsPGVectorStoreIteratorWithCorrectParams() {
        var iterator = provider.getFileIterator();
        assertThat(iterator).isInstanceOf(PGVectorStoreIterator.class);
        // Optionally, use reflection to check fields if needed
    }

    @Test
    void constructor_setsAllFieldsCorrectly() {
        assertThat(provider).extracting(
                "storeConfiguration",
                "pGVectorStoreConnection",
                "storeName",
                "queryParams",
                "dimension",
                "createStore"
        ).containsExactly(
                storeConfiguration,
                pgVectorStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }
} 
