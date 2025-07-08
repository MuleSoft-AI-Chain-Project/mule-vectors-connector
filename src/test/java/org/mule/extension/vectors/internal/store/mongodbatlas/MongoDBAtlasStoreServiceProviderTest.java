package org.mule.extension.vectors.internal.store.mongodbatlas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoDBAtlasStoreServiceProviderTest {
    private StoreConfiguration storeConfiguration;
    private MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection;
    private QueryParameters queryParameters;

    @BeforeEach
    void setUp() {
        storeConfiguration = mock(StoreConfiguration.class);
        mongoDBAtlasStoreConnection = mock(MongoDBAtlasStoreConnection.class);
        queryParameters = mock(QueryParameters.class);
        // Add mocks for MongoDB chain
        MongoClient mongoClient = mock(MongoClient.class);
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> collection = mock(MongoCollection.class);
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(mongoDBAtlasStoreConnection.getMongoClient()).thenReturn(mongoClient);
        when(mongoDBAtlasStoreConnection.getDatabase()).thenReturn("testdb");
        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString())).thenReturn(collection);
        when(queryParameters.pageSize()).thenReturn(2);
        when(queryParameters.retrieveEmbeddings()).thenReturn(true);
        when(collection.find()).thenReturn(findIterable);
        when(findIterable.skip(anyInt())).thenReturn(findIterable);
        when(findIterable.limit(anyInt())).thenReturn(findIterable);
        when(findIterable.into(any(List.class))).thenReturn(new ArrayList<>());
    }

    @Test
    void getService_returnsMongoDBAtlasStore() {
        MongoDBAtlasStoreServiceProvider provider = new MongoDBAtlasStoreServiceProvider(
                storeConfiguration, mongoDBAtlasStoreConnection, "store", queryParameters, 42, true);
        VectorStoreService service = provider.getService();
        assertThat(service).isInstanceOf(MongoDBAtlasStore.class);
    }

    @Test
    void getFileIterator_returnsMongoDBAtlasStoreIterator() {
        MongoDBAtlasStoreServiceProvider provider = new MongoDBAtlasStoreServiceProvider(
                storeConfiguration, mongoDBAtlasStoreConnection, "store", queryParameters, 42, true);
        VectoreStoreIterator iterator = provider.getFileIterator();
        assertThat(iterator).isInstanceOf(MongoDBAtlasStoreIterator.class);
    }
}
