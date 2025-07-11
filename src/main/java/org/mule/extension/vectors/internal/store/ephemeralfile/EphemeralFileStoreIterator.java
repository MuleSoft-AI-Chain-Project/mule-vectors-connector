package org.mule.extension.vectors.internal.store.ephemeralfile;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mule.extension.vectors.internal.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.NoSuchElementException;

public class EphemeralFileStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EphemeralFileStoreIterator.class);

  private final String storeFilePath;
  private final QueryParameters queryParams;
  private final JSONArray entries;
  private int currentIndex = 0;
  private String storeName;

  public EphemeralFileStoreIterator(
      EphemeralFileStoreConnection ephemeralFileStoreConnection,
      QueryParameters queryParams,
      String storeName
  ) {
    this.storeFilePath = ephemeralFileStoreConnection.getWorkingDir();
    this.queryParams = queryParams;
    this.storeName = storeName;
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
  public String getEphemeralFileStorePath() {
    return (storeFilePath != null && !storeFilePath.isBlank() ? storeFilePath + "/" : "") + storeName + ".store";
  }
  @Override
  public boolean hasNext() {
    return currentIndex < this.entries.length();
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    try {
      JSONObject entry = this.entries.getJSONObject(currentIndex++);

      String embeddingId = entry.getString("id");
      JSONObject metadataObject = entry
          .getJSONObject("embedded")
          .getJSONObject("metadata")
          .getJSONObject("metadata");

      String text = entry
          .getJSONObject("embedded")
          .getString("text");

      float[] vector = null;
      if (queryParams.retrieveEmbeddings()) {
        JSONArray vectorJsonArray = entry
            .getJSONObject("embedding")
            .getJSONArray("vector");
        vector = new float[vectorJsonArray.length()];
        for (int i = 0; i < vectorJsonArray.length(); i++) {
          vector[i] = (float) vectorJsonArray.getDouble(i);
        }
      }

      // This is the only place you may want to adapt for Embedded type.
      // If you want to keep it generic, you can cast or use a factory.
      // For now, we keep it as TextSegment to match the original.
      @SuppressWarnings("unchecked")
      Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataObject.toMap()));

      return new VectorStoreRow<>(
          embeddingId,
          vector != null ? new Embedding(vector) : null,
          embedded
      );

    } catch (Exception e) {
      LOGGER.error("Error while fetching next row", e);
      throw new NoSuchElementException("No more elements available");
    }
  }
}
