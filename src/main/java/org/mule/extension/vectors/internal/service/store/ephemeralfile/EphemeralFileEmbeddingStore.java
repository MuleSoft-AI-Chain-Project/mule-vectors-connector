package org.mule.extension.vectors.internal.service.store.ephemeralfile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.SneakyThrows;

public class EphemeralFileEmbeddingStore implements EmbeddingStore<TextSegment> {

    private String ephemeralFileStorePath;

    public EphemeralFileEmbeddingStore(String ephemeralFileStorePath) {

        this.ephemeralFileStorePath = ephemeralFileStorePath;
    }

    private InMemoryEmbeddingStore<TextSegment> loadEmbeddingStore() {
        
        InMemoryEmbeddingStore<TextSegment> embeddingStore;
        if (Files.exists(Paths.get(this.ephemeralFileStorePath))) {

            embeddingStore = InMemoryEmbeddingStore.fromFile(ephemeralFileStorePath);
        } else {

            embeddingStore = new InMemoryEmbeddingStore<>();
        }
        return embeddingStore;
    }

    @Override
    public String add(Embedding embedding) {
        
        String id = null;
        synchronized(this) {

            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            id = embeddingStore.add(embedding);
            embeddingStore.serializeToFile(ephemeralFileStorePath);    
        }
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        
        synchronized(this) {
         
            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            embeddingStore.add(id, embedding);
            embeddingStore.serializeToFile(ephemeralFileStorePath);
        }
    }

    @Override
    public String add(Embedding embedding, TextSegment embedded) {
        
        String id = null;
        synchronized(this) {

            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            id = embeddingStore.add(embedding, embedded);
            embeddingStore.serializeToFile(ephemeralFileStorePath);    
        }
        return id;
    }

    public void add(String id, Embedding embedding, TextSegment embedded) {

        synchronized(this) {

            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            embeddingStore.add(id, embedding, embedded);
            embeddingStore.serializeToFile(ephemeralFileStorePath);
        }
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        
        List<String> newEntries = null;
        synchronized(this) {

            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            newEntries = embeddingStore.addAll(embeddings);
            embeddingStore.serializeToFile(ephemeralFileStorePath); 
        }
        return newEntries;
    }

    @Override
    public void addAll(List<String> ids, List<Embedding> embeddings, List<TextSegment> embedded) {

        synchronized(this) {

            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            embeddingStore.addAll(ids, embeddings, embedded);
            embeddingStore.serializeToFile(ephemeralFileStorePath); 
        }
    }

    @Override
    public void removeAll(Collection<String> ids) {

        synchronized(this) {

            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            embeddingStore.removeAll(ids);
            embeddingStore.serializeToFile(ephemeralFileStorePath); 
        }
    }

    @Override
    public void removeAll(Filter filter) {

        synchronized(this) {

            InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
            embeddingStore.removeAll(filter);
            embeddingStore.serializeToFile(ephemeralFileStorePath); 
        }
    }

    @SneakyThrows
    @Override
    public void removeAll() {

        synchronized(this) {
                Files.deleteIfExists(Paths.get(ephemeralFileStorePath));

        }
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        
        InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
        return embeddingStore.search(request);
    }
    
    public String serializeToJson() {

        InMemoryEmbeddingStore<TextSegment> embeddingStore = loadEmbeddingStore();
        return embeddingStore.serializeToJson();
    }
}
