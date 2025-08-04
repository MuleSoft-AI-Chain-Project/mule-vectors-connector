package org.mule.extension.vectors.internal.helper.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile.EphemeralFileStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.milvus.MilvusStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.pinecone.PineconeStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit tests for ConnectionValidationStrategies class.
 */
public class ConnectionValidationStrategiesTest {

  // ============ AlloyDB Tests ============

  @Test
  public void testValidateAlloyDB_AllValidParameters_ShouldPass() {
    // Given
    AlloyDBStoreConnectionParameters params = mock(AlloyDBStoreConnectionParameters.class);
    when(params.getProjectId()).thenReturn("test-project");
    when(params.getRegion()).thenReturn("us-central1");
    when(params.getCluster()).thenReturn("test-cluster");
    when(params.getInstance()).thenReturn("test-instance");
    when(params.getIamAccountEmail()).thenReturn("test@example.com");
    when(params.getHost()).thenReturn("localhost");
    when(params.getPort()).thenReturn(5432);
    when(params.getDatabase()).thenReturn("testdb");
    when(params.getUser()).thenReturn("testuser");
    when(params.getPassword()).thenReturn("testpass");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateAlloyDB(params);
    });
  }

  @Test
  public void testValidateAlloyDB_MissingProjectId_ShouldThrowException() {
    // Given
    AlloyDBStoreConnectionParameters params = mock(AlloyDBStoreConnectionParameters.class);
    when(params.getProjectId()).thenReturn(null);

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateAlloyDB(params);
    });

    assertTrue(exception.getMessage().contains("Project ID is required for AlloyDB connection"));
  }

  @Test
  public void testValidateAlloyDB_InvalidPort_ShouldThrowException() {
    // Given
    AlloyDBStoreConnectionParameters params = mock(AlloyDBStoreConnectionParameters.class);
    when(params.getProjectId()).thenReturn("test-project");
    when(params.getRegion()).thenReturn("us-central1");
    when(params.getCluster()).thenReturn("test-cluster");
    when(params.getInstance()).thenReturn("test-instance");
    when(params.getIamAccountEmail()).thenReturn("test@example.com");
    when(params.getHost()).thenReturn("localhost");
    when(params.getPort()).thenReturn(0); // Invalid port

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateAlloyDB(params);
    });

    assertTrue(exception.getMessage().contains("Port is required for AlloyDB connection and must be > 0"));
  }

  // ============ PGVector Tests ============

  @Test
  public void testValidatePGVector_AllValidParameters_ShouldPass() {
    // Given
    PGVectorStoreConnectionParameters params = mock(PGVectorStoreConnectionParameters.class);
    when(params.getHost()).thenReturn("localhost");
    when(params.getPort()).thenReturn(5432);
    when(params.getDatabase()).thenReturn("testdb");
    when(params.getUser()).thenReturn("testuser");
    when(params.getPassword()).thenReturn("testpass");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validatePGVector(params);
    });
  }

  @Test
  public void testValidatePGVector_MissingHost_ShouldThrowException() {
    // Given
    PGVectorStoreConnectionParameters params = mock(PGVectorStoreConnectionParameters.class);
    when(params.getHost()).thenReturn(null);

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validatePGVector(params);
    });

    assertTrue(exception.getMessage().contains("Host is required for PGVector connection"));
  }

  // ============ Weaviate Tests ============

  @Test
  public void testValidateWeaviate_AllValidParameters_ShouldPass() {
    // Given
    WeaviateStoreConnectionParameters params = mock(WeaviateStoreConnectionParameters.class);
    when(params.getScheme()).thenReturn("https");
    when(params.getHost()).thenReturn("localhost");
    when(params.getApiKey()).thenReturn("test-api-key");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateWeaviate(params);
    });
  }

  @Test
  public void testValidateWeaviate_MissingScheme_ShouldThrowException() {
    // Given
    WeaviateStoreConnectionParameters params = mock(WeaviateStoreConnectionParameters.class);
    when(params.getScheme()).thenReturn("");
    when(params.getHost()).thenReturn("localhost");
    when(params.getApiKey()).thenReturn("test-api-key");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateWeaviate(params);
    });

    assertTrue(exception.getMessage().contains("Scheme is required for Weaviate connection"));
  }

  // ============ MongoDB Atlas Tests ============

  @Test
  public void testValidateMongoDBAtlas_AllValidParameters_ShouldPass() {
    // Given
    MongoDBAtlasStoreConnectionParameters params = mock(MongoDBAtlasStoreConnectionParameters.class);
    when(params.getHost()).thenReturn("localhost");
    when(params.getUser()).thenReturn("testuser");
    when(params.getPassword()).thenReturn("testpass");
    when(params.getDatabase()).thenReturn("testdb");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateMongoDBAtlas(params);
    });
  }

  @Test
  public void testValidateMongoDBAtlas_MissingDatabase_ShouldThrowException() {
    // Given
    MongoDBAtlasStoreConnectionParameters params = mock(MongoDBAtlasStoreConnectionParameters.class);
    when(params.getHost()).thenReturn("localhost");
    when(params.getUser()).thenReturn("testuser");
    when(params.getPassword()).thenReturn("testpass");
    when(params.getDatabase()).thenReturn(null);

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateMongoDBAtlas(params);
    });

    assertTrue(exception.getMessage().contains("Database is required for MongoDB Atlas connection"));
  }

  // ============ OpenSearch Tests ============

  @Test
  public void testValidateOpenSearch_WithPassword_ShouldPass() {
    // Given
    OpenSearchStoreConnectionParameters params = mock(OpenSearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("https://localhost:9200");
    when(params.getPassword()).thenReturn("testpass");
    when(params.getApiKey()).thenReturn(null);

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateOpenSearch(params);
    });
  }

  @Test
  public void testValidateOpenSearch_WithApiKey_ShouldPass() {
    // Given
    OpenSearchStoreConnectionParameters params = mock(OpenSearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("https://localhost:9200");
    when(params.getPassword()).thenReturn(null);
    when(params.getApiKey()).thenReturn("test-api-key");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateOpenSearch(params);
    });
  }

  @Test
  public void testValidateOpenSearch_MissingBothPasswordAndApiKey_ShouldThrowException() {
    // Given
    OpenSearchStoreConnectionParameters params = mock(OpenSearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("https://localhost:9200");
    when(params.getPassword()).thenReturn(null);
    when(params.getApiKey()).thenReturn(null);

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateOpenSearch(params);
    });

    assertTrue(exception.getMessage().contains("Either password or API Key is required for OpenSearch connection"));
  }

  // ============ Elasticsearch Tests ============

  @Test
  public void testValidateElasticsearch_AllValidParameters_ShouldPass() {
    // Given
    ElasticsearchStoreConnectionParameters params = mock(ElasticsearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("https://localhost:9200");
    when(params.getPassword()).thenReturn("testpass");
    when(params.getApiKey()).thenReturn(null);

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateElasticsearch(params);
    });
  }

  @Test
  public void testValidateElasticsearch_MissingUrl_ShouldThrowException() {
    // Given
    ElasticsearchStoreConnectionParameters params = mock(ElasticsearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn(null);
    when(params.getPassword()).thenReturn("testpass");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateElasticsearch(params);
    });

    assertTrue(exception.getMessage().contains("URL is required for Elasticsearch connection"));
  }

  // ============ Chroma Tests ============

  @Test
  public void testValidateChroma_ValidUrl_ShouldPass() {
    // Given
    ChromaStoreConnectionParameters params = mock(ChromaStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("http://localhost:8000");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateChroma(params);
    });
  }

  @Test
  public void testValidateChroma_MissingUrl_ShouldThrowException() {
    // Given
    ChromaStoreConnectionParameters params = mock(ChromaStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateChroma(params);
    });

    assertTrue(exception.getMessage().contains("URL is required for Chroma connection"));
  }

  // ============ Pinecone Tests ============

  @Test
  public void testValidatePinecone_AllValidParameters_ShouldPass() {
    // Given
    PineconeStoreConnectionParameters params = mock(PineconeStoreConnectionParameters.class);
    when(params.getApiKey()).thenReturn("test-api-key");
    when(params.getCloud()).thenReturn("aws");
    when(params.getRegion()).thenReturn("us-east-1");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validatePinecone(params);
    });
  }

  @Test
  public void testValidatePinecone_MissingCloud_ShouldThrowException() {
    // Given
    PineconeStoreConnectionParameters params = mock(PineconeStoreConnectionParameters.class);
    when(params.getApiKey()).thenReturn("test-api-key");
    when(params.getCloud()).thenReturn(null);
    when(params.getRegion()).thenReturn("us-east-1");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validatePinecone(params);
    });

    assertTrue(exception.getMessage().contains("Cloud is required for Pinecone connection"));
  }

  // ============ Qdrant Tests ============

  @Test
  public void testValidateQdrant_AllValidParameters_ShouldPass() {
    // Given
    QdrantStoreConnectionParameters params = mock(QdrantStoreConnectionParameters.class);
    when(params.getHost()).thenReturn("localhost");
    when(params.getGprcPort()).thenReturn(6334);
    when(params.getApiKey()).thenReturn("test-api-key");
    when(params.getTextSegmentKey()).thenReturn("text");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateQdrant(params);
    });
  }

  @Test
  public void testValidateQdrant_InvalidGrpcPort_ShouldThrowException() {
    // Given
    QdrantStoreConnectionParameters params = mock(QdrantStoreConnectionParameters.class);
    when(params.getHost()).thenReturn("localhost");
    when(params.getGprcPort()).thenReturn(-1);
    when(params.getApiKey()).thenReturn("test-api-key");
    when(params.getTextSegmentKey()).thenReturn("text");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateQdrant(params);
    });

    assertTrue(exception.getMessage().contains("gprcPort is required for Qdrant connection and must be > 0"));
  }

  // ============ Milvus Tests ============

  @Test
  public void testValidateMilvus_AllValidParameters_ShouldPass() {
    // Given
    MilvusStoreConnectionParameters params = mock(MilvusStoreConnectionParameters.class);
    when(params.getUri()).thenReturn("https://localhost:19530");
    when(params.getToken()).thenReturn("test-token");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateMilvus(params);
    });
  }

  @Test
  public void testValidateMilvus_MissingToken_ShouldThrowException() {
    // Given
    MilvusStoreConnectionParameters params = mock(MilvusStoreConnectionParameters.class);
    when(params.getUri()).thenReturn("https://localhost:19530");
    when(params.getToken()).thenReturn("");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateMilvus(params);
    });

    assertTrue(exception.getMessage().contains("Token is required for Milvus connection"));
  }

  // ============ AI Search Tests ============

  @Test
  public void testValidateAISearch_AllValidParameters_ShouldPass() {
    // Given
    AISearchStoreConnectionParameters params = mock(AISearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("https://test.search.windows.net");
    when(params.getApiKey()).thenReturn("test-api-key");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateAISearch(params);
    });
  }

  @Test
  public void testValidateAISearch_MissingApiKey_ShouldThrowException() {
    // Given
    AISearchStoreConnectionParameters params = mock(AISearchStoreConnectionParameters.class);
    when(params.getUrl()).thenReturn("https://test.search.windows.net");
    when(params.getApiKey()).thenReturn(null);

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateAISearch(params);
    });

    assertTrue(exception.getMessage().contains("API Key is required for AI Search connection"));
  }

  // ============ Ephemeral File Tests ============

  @Test
  public void testValidateEphemeralFile_ValidWorkingDir_ShouldPass() {
    // Given
    EphemeralFileStoreConnectionParameters params = mock(EphemeralFileStoreConnectionParameters.class);
    when(params.getWorkingDir()).thenReturn("/tmp/vector-store");

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ConnectionValidationStrategies.validateEphemeralFile(params);
    });
  }

  @Test
  public void testValidateEphemeralFile_MissingWorkingDir_ShouldThrowException() {
    // Given
    EphemeralFileStoreConnectionParameters params = mock(EphemeralFileStoreConnectionParameters.class);
    when(params.getWorkingDir()).thenReturn("   ");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateEphemeralFile(params);
    });

    assertTrue(exception.getMessage().contains("Working Directory is required for Ephemeral File connection"));
  }

  // ============ Edge Cases Tests ============

  @Test
  public void testValidationStrategies_WithBlankStrings_ShouldThrowException() {
    // Given
    PGVectorStoreConnectionParameters params = mock(PGVectorStoreConnectionParameters.class);
    when(params.getHost()).thenReturn("  "); // Blank string
    when(params.getPort()).thenReturn(5432);
    when(params.getDatabase()).thenReturn("testdb");
    when(params.getUser()).thenReturn("testuser");
    when(params.getPassword()).thenReturn("testpass");

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validatePGVector(params);
    });

    assertTrue(exception.getMessage().contains("Host is required for PGVector connection"));
  }

  @Test
  public void testValidationStrategies_WithEmptyStrings_ShouldThrowException() {
    // Given
    WeaviateStoreConnectionParameters params = mock(WeaviateStoreConnectionParameters.class);
    when(params.getScheme()).thenReturn("https");
    when(params.getHost()).thenReturn("localhost");
    when(params.getApiKey()).thenReturn(""); // Empty string

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ConnectionValidationStrategies.validateWeaviate(params);
    });

    assertTrue(exception.getMessage().contains("API Key is required for Weaviate connection"));
  }
}
