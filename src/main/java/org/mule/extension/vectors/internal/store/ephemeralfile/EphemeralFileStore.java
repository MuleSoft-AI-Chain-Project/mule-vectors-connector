package org.mule.extension.vectors.internal.store.ephemeralfile;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

import java.util.NoSuchElementException;

/**
 * EphemeralFileStore is a specialized implementation of {@link BaseStore} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class EphemeralFileStore extends BaseStore {

  private String workingDir;
  /**
   * Initializes a new instance of EphemeralFileStore.
   *
   * @param storeName     the name of the vector store.
   * @param storeConfiguration the configuration object containing necessary settings.
   * @param queryParams   parameters related to query configurations.
   */
  public EphemeralFileStore(StoreConfiguration storeConfiguration, EphemeralFileStoreConnection ephemeralFileStoreConnection, String storeName, QueryParameters queryParams, int dimension) {

    super(storeConfiguration, ephemeralFileStoreConnection, storeName, queryParams, dimension, true);
    this.workingDir = ephemeralFileStoreConnection.getWorkingDir();
  }

  public String getEphemeralFileStorePath() {
    
    return (workingDir != null && !workingDir.isBlank() ? workingDir + "/" : "") + storeName + ".store";
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    return new EphemeralFileEmbeddingStore(getEphemeralFileStorePath());
  }

  @Override
  public EphemeralFileStore.RowIterator rowIterator() {
    try {
      return new EphemeralFileStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {

    private JSONArray entries;

    public RowIterator() throws Exception {

      super();
      EphemeralFileEmbeddingStore ephemeralFileEmbeddingStore = (EphemeralFileEmbeddingStore) buildEmbeddingStore();
      String jsonSerializedStore = ephemeralFileEmbeddingStore.serializeToJson();
      JSONObject jsonObject = new JSONObject(jsonSerializedStore);
      this.entries = jsonObject.getJSONArray("entries");
    }

    @Override
    public boolean hasNext() {
      return this.entries.length() > 0;
    }

    @Override
    public Row<?> next() {

      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      try {

        JSONObject entry = this.entries.getJSONObject(0);
        this.entries.remove(0);

        String embeddingId = entry
            .getString("id");
        JSONObject metadataObject =   entry
        .getJSONObject("embedded")
        .getJSONObject("metadata")
        .getJSONObject("metadata");

        String text = entry
            .getJSONObject("embedded")
            .getString("text");
      
        JSONArray vectorJsonArray = entry
            .getJSONObject("embedding")
            .getJSONArray("vector");
        // Convert to float[]
        float[] vector = new float[vectorJsonArray.length()];
        for (int i = 0; i < vectorJsonArray.length(); i++) {
            vector[i] = (float) vectorJsonArray.getDouble(i); // casting double to float
        }

        return new Row<>(embeddingId,
                         vector != null ? new Embedding(vector) : null,
                         new TextSegment(text, Metadata.from(metadataObject.toMap())));

      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        throw new NoSuchElementException("No more elements available");
      }
    }
  }
}
