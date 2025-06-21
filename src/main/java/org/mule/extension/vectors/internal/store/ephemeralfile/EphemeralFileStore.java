package org.mule.extension.vectors.internal.store.ephemeralfile;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * EphemeralFileStore is a specialized implementation of {@link BaseStoreService} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class EphemeralFileStore extends BaseStoreService {

    private final String workingDir;
    private final QueryParameters queryParams;

    /**
     * Initializes a new instance of EphemeralFileStore.
     *
     * @param storeConfiguration       the configuration object containing necessary settings.
     * @param ephemeralFileStoreConnection the ephemeral file store connection.
     * @param storeName                the name of the vector store.
     * @param queryParams              parameters related to query configurations.
     * @param dimension                the dimension of the vectors.
     * @param createStore              flag to create the store if it does not exist.
     */
    public EphemeralFileStore(StoreConfiguration storeConfiguration, EphemeralFileStoreConnection ephemeralFileStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
        super(storeConfiguration, ephemeralFileStoreConnection, storeName, dimension, createStore);
        this.workingDir = ephemeralFileStoreConnection.getWorkingDir();
        this.queryParams = queryParams;
    }

    public String getEphemeralFileStorePath() {
        return (workingDir != null && !workingDir.isBlank() ? workingDir + "/" : "") + storeName + ".store";
    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        try {
            return new EphemeralFileEmbeddingStore(getEphemeralFileStorePath());
        } catch (Exception e) {
            throw new ModuleException("Failed to build Ephemeral File store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
        }
    }

    @Override
    public Iterator<BaseStoreService.Row<?>> getRowIterator() {
        try {
            return new EphemeralFileStore.RowIterator();
        } catch (ModuleException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while creating row iterator", e);
            throw new ModuleException("Failed to create iterator for Ephemeral File store", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
        }
    }

    public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

        private final JSONArray entries;
        private int currentIndex = 0;

        public RowIterator() {
            try {
                EphemeralFileEmbeddingStore ephemeralFileEmbeddingStore = new EphemeralFileEmbeddingStore(getEphemeralFileStorePath());
                String jsonSerializedStore = ephemeralFileEmbeddingStore.serializeToJson();
                JSONObject jsonObject = new JSONObject(jsonSerializedStore);
                this.entries = jsonObject.getJSONArray("entries");
            } catch (JSONException e) {
                throw new ModuleException("Invalid file format, failed to parse JSON: " + e.getMessage(), MuleVectorsErrorType.INVALID_FILE_FORMAT, e);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof NoSuchFileException) {
                    throw new ModuleException("Store file not found: " + e.getMessage(), MuleVectorsErrorType.STORE_NOT_FOUND, e);
                } else if (e.getCause() instanceof IOException) {
                    throw new ModuleException("Failed to read store file: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
                } else {
                    throw new ModuleException("An unexpected error occurred while reading the store file.", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return currentIndex < this.entries.length();
        }

        @Override
        public BaseStoreService.Row<?> next() {

            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            try {

                JSONObject entry = this.entries.getJSONObject(currentIndex++);

                String embeddingId = entry
                        .getString("id");
                JSONObject metadataObject = entry
                        .getJSONObject("embedded")
                        .getJSONObject("metadata")
                        .getJSONObject("metadata");

                String text = entry
                        .getJSONObject("embedded")
                        .getString("text");

                float[] vector = null;
                if(queryParams.retrieveEmbeddings()) {
                    JSONArray vectorJsonArray = entry
                            .getJSONObject("embedding")
                            .getJSONArray("vector");
                    // Convert to float[]
                    vector = new float[vectorJsonArray.length()];
                    for (int i = 0; i < vectorJsonArray.length(); i++) {
                        vector[i] = (float) vectorJsonArray.getDouble(i); // casting double to float
                    }
                }


                return new BaseStoreService.Row<>(embeddingId,
                        vector != null ? new Embedding(vector) : null,
                        new TextSegment(text, Metadata.from(metadataObject.toMap())));

            } catch (Exception e) {
                LOGGER.error("Error while fetching next row", e);
                throw new NoSuchElementException("No more elements available");
            }
        }
    }
}
