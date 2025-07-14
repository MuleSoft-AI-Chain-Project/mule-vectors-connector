package org.mule.extension.vectors.internal.service.store.ephemeralfile;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;


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

}
