package org.mule.extension.vectors.internal.store.mongodbatlas;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.mongodbatlas.MongoDBAtlasStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.*;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.langchain4j.data.segment.TextSegment;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoDBAtlasStoreIteratorTest {

  @Mock
  MongoDBAtlasStoreConnection connection;
  @Mock
  MongoClient mongoClient;
  @Mock
  MongoDatabase mongoDatabase;
  @Mock
  MongoCollection<Document> collection;
  @Mock
  QueryParameters queryParameters;
  @Mock
  FindIterable<Document> findIterable;

  @BeforeEach
  void setUp() {
    lenient().when(connection.getMongoClient()).thenReturn(mongoClient);
    lenient().when(connection.getDatabase()).thenReturn("testdb");
    lenient().when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
    lenient().when(mongoDatabase.getCollection(anyString())).thenReturn(collection);
    lenient().when(queryParameters.pageSize()).thenReturn(2);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    // Default: all find() returns findIterable, and skip/limit chain
    lenient().when(collection.find()).thenReturn(findIterable);
    lenient().when(findIterable.skip(anyInt())).thenReturn(findIterable);
    lenient().when(findIterable.limit(anyInt())).thenReturn(findIterable);
  }

  @Test
  void constructor_loadsFirstBatch() {
    List<Document> docs = List.of(new Document("_id", "id1").append("text", "t1").append("metadata", new Document()),
                                  new Document("_id", "id2").append("text", "t2").append("metadata", new Document()));
    when(findIterable.into(any(List.class)))
        .thenAnswer(inv -> {
          List<Document> l = inv.getArgument(0);
          l.addAll(docs);
          return l;
        })
        .thenAnswer(inv -> {
          List<Document> l = inv.getArgument(0);
          return l;
        }); // empty batch after first
    MongoDBAtlasStoreIterator<TextSegment> iterator = new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("id1");
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("id2");
    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  void paging_loadsMultipleBatches() {
    List<Document> batch1 = List.of(new Document("_id", "id1").append("text", "t1").append("metadata", new Document()));
    List<Document> batch2 = List.of(new Document("_id", "id2").append("text", "t2").append("metadata", new Document()));
    when(findIterable.into(any(List.class))).thenAnswer(inv -> {
      List<Document> l = inv.getArgument(0);
      l.addAll(batch1);
      return l;
    })
        .thenAnswer(inv -> {
          List<Document> l = inv.getArgument(0);
          l.addAll(batch2);
          return l;
        })
        .thenAnswer(inv -> {
          List<Document> l = inv.getArgument(0);
          return l;
        }); // empty batch
    MongoDBAtlasStoreIterator<TextSegment> iterator = new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("id1");
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("id2");
    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  void next_whenNoMoreElements_throwsNoSuchElementException() {
    when(findIterable.into(any(List.class))).thenReturn(new ArrayList<>());
    MongoDBAtlasStoreIterator<TextSegment> iterator = new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters);
    assertThat(iterator.hasNext()).isFalse();
    assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void loadNextBatch_whenMongoSocketOpenException_throwsModuleException() {
    when(collection.find()).thenThrow(new MongoSocketOpenException("fail", null));
    assertThatThrownBy(() -> new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("MongoDB connection failed");
  }

  @Test
  void loadNextBatch_whenMongoSocketReadException_throwsModuleException() {
    when(collection.find()).thenThrow(new MongoSocketReadException("fail", null));
    assertThatThrownBy(() -> new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("MongoDB connection failed");
  }

  @Test
  void loadNextBatch_whenMongoSecurityException_throwsModuleException() {
    when(collection.find()).thenThrow(new com.mongodb.MongoSecurityException(null, "fail", null));
    assertThatThrownBy(() -> new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("MongoDB authentication failed");
  }

  @Test
  void loadNextBatch_whenMongoCommandException_throwsModuleException() {
    when(collection.find()).thenThrow(new MongoCommandException(new org.bson.BsonDocument(), null));
    assertThatThrownBy(() -> new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("MongoDB query failed");
  }

  @Test
  void loadNextBatch_whenMongoException_throwsModuleException() {
    when(collection.find()).thenThrow(new MongoException("fail"));
    assertThatThrownBy(() -> new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Error fetching from MongoDB");
  }

  @Test
  void next_extractsVectorAndMetadata() {
    List<Double> vectorList = Arrays.asList(0.1, 0.2, 0.3);
    Document metadata = new Document("foo", "bar");
    Document doc = new Document("_id", "id1")
        .append("text", "hello")
        .append("metadata", metadata)
        .append("embedding", vectorList);
    when(findIterable.into(any(List.class))).thenAnswer(inv -> {
      List<Document> l = inv.getArgument(0);
      l.add(doc);
      return l;
    });
    MongoDBAtlasStoreIterator<TextSegment> iterator = new MongoDBAtlasStoreIterator<>(connection, "store", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    var row = iterator.next();
    assertThat(row.getId()).isEqualTo("id1");
    assertThat(row.getEmbedding().vector()).containsExactly(0.1f, 0.2f, 0.3f);
    assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("hello");
    assertThat(((TextSegment) row.getEmbedded()).metadata().containsKey("foo")).isTrue();
  }
}
