package org.mule.extension.vectors.internal.store.opensearch;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class OpenSearchStore extends BaseStoreService {

    static final String TEXT_DEFAULT_FIELD_NAME = "text";
    static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
    static final String VECTOR_DEFAULT_FIELD_NAME = "vector";

    private final String url;
    private final OpenSearchClient openSearchClient;
    private final QueryParameters queryParams;

    public OpenSearchStore(StoreConfiguration storeConfiguration, OpenSearchStoreConnection openSearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

        super(storeConfiguration, openSearchStoreConnection, storeName, dimension, createStore);

        this.url = openSearchStoreConnection.getUrl();
        this.openSearchClient = openSearchStoreConnection.getOpenSearchClient();
        this.queryParams = queryParams;
    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        return OpenSearchEmbeddingStore.builder()
                .serverUrl(url)
                .openSearchClient(openSearchClient)
                .indexName(storeName)
                .build();
    }

    @Override
    public Iterator<BaseStoreService.Row<?>> getRowIterator() {
        try {
            return new OpenSearchStore.RowIterator();
        } catch (Exception e) {
            LOGGER.error("Error while creating row iterator", e);
            throw new RuntimeException(e);
        }
    }

    public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

        private String scrollId;
        private List<Hit<Map>> currentBatch;
        private int currentIndex;

        public RowIterator() throws IOException, URISyntaxException {
            this.currentBatch = new ArrayList<>();
            this.currentIndex = 0;
            fetchNextBatch();
        }

        @Override
        public boolean hasNext() {
            return currentIndex < currentBatch.size() || fetchNextBatch();
        }

        @Override
        public BaseStoreService.Row<?> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Hit<Map> hit = currentBatch.get(currentIndex++);
            String embeddingId = hit.id();
            Map<String, Object> sourceMap = hit.source();
            float[] vector = null;
            if (queryParams.retrieveEmbeddings()) {
                List<Double> vectorList = (List<Double>) sourceMap.get(VECTOR_DEFAULT_FIELD_NAME);
                if (vectorList != null) {
                    vector = new float[vectorList.size()];
                    for (int i = 0; i < vectorList.size(); i++) {
                        vector[i] = vectorList.get(i).floatValue();
                    }
                }
            }
            String text = (String) sourceMap.get(TEXT_DEFAULT_FIELD_NAME);
            JSONObject metadataObject = new JSONObject((Map) sourceMap.get(METADATA_DEFAULT_FIELD_NAME));

            return new BaseStoreService.Row<TextSegment>(embeddingId,
                    vector != null ? new Embedding(vector) : null,
                    new TextSegment(text, Metadata.from(metadataObject.toMap())));
        }

        private boolean fetchNextBatch() {
            try {
                if (scrollId == null) {
                    SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                            .index(storeName)
                            .size((int) queryParams.pageSize())
                            .scroll(Time.of(t -> t.time("1m")));

                    if (queryParams.retrieveEmbeddings()) {
                        searchRequestBuilder.source(s -> s.filter(f -> f.includes(TEXT_DEFAULT_FIELD_NAME,
                                METADATA_DEFAULT_FIELD_NAME,
                                VECTOR_DEFAULT_FIELD_NAME)));
                    } else {
                        searchRequestBuilder.source(s -> s.filter(f -> f.includes(TEXT_DEFAULT_FIELD_NAME,
                                METADATA_DEFAULT_FIELD_NAME)));
                    }

                    SearchRequest searchRequest = searchRequestBuilder.build();
                    SearchResponse<Map> searchResponse = openSearchClient.search(searchRequest, Map.class);
                    currentBatch = searchResponse.hits().hits();
                    scrollId = searchResponse.scrollId();
                } else {
                    ScrollRequest scrollRequest = new ScrollRequest.Builder()
                            .scrollId(scrollId)
                            .scroll(Time.of(t -> t.time("1m")))
                            .build();
                    ScrollResponse<Map> scrollResponse = openSearchClient.scroll(scrollRequest, Map.class);
                    currentBatch = scrollResponse.hits().hits();
                    scrollId = scrollResponse.scrollId();
                }
                currentIndex = 0;
                if (currentBatch.isEmpty()) {
                    close();
                    return false;
                }
                return true;
            } catch (IOException e) {
                LOGGER.error("Error while fetching next batch", e);
                return false;
            }
        }

        public void close() {
            if (scrollId != null) {
                try {
                    openSearchClient.clearScroll(builder -> builder.scrollId(scrollId));
                } catch (IOException e) {
                    LOGGER.error("Failed to clear scroll context", e);
                }
            }
        }
    }
}
