package org.mule.extension.vectors.internal.storage.azureblob;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.InputStream;
import java.util.Iterator;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureBlobStorage extends BaseStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobStorage.class);

    private final String azureName;
    private final String azureKey;

    private StorageSharedKeyCredential getCredentials() {
        return new StorageSharedKeyCredential(azureName, azureKey);
    }

    private BlobServiceClient blobServiceClient;

    private BlobServiceClient getBlobServiceClient() {

        if(this.blobServiceClient == null) {

            // Azure SDK client builders accept the credential as a parameter
            this.blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net/", azureName))
                .credential(getCredentials())
                .buildClient();
        }
        return this.blobServiceClient;
    }

    private Iterator<BlobItem> blobIterator;

    private Iterator<BlobItem> getBlobIterator() {

        if(blobIterator == null) {

            // Get a BlobContainerClient
            BlobContainerClient containerClient = getBlobServiceClient().getBlobContainerClient(getContainerName());
            // Get an iterator for all blobs in the container
            this.blobIterator = containerClient.listBlobs(new ListBlobsOptions().setPrefix(getBlobName()),null).iterator();

        }
        return blobIterator;
    }

    private String getContainerName() {

        String azureBlobStorageUrl = this.contextPath;
        String endpoint = String.format("https://%s.blob.core.windows.net/", azureName);
        // Remove the "s3://" prefix
        if (azureBlobStorageUrl.startsWith(endpoint)) {
            azureBlobStorageUrl = azureBlobStorageUrl.substring(endpoint.length());
        }
        // Extract the bucket name
        String bucket = azureBlobStorageUrl.contains("/") ? azureBlobStorageUrl.substring(0, azureBlobStorageUrl.indexOf("/")) : azureBlobStorageUrl;
        return bucket;
    }

    private String getBlobName() {

        String azureBlobStorageUrl = this.contextPath;
        String endpoint = String.format("https://%s.blob.core.windows.net/", azureName);
        // Remove the "s3://" prefix
        if (azureBlobStorageUrl.startsWith(endpoint)) {
            azureBlobStorageUrl = azureBlobStorageUrl.substring(endpoint.length());
        }
        // Extract the bucket name and object key
        int slashIndex = azureBlobStorageUrl.indexOf("/");
        String objectKey = slashIndex != -1 ? azureBlobStorageUrl.substring(slashIndex + 1) : "";
        return objectKey;
    }

    public AzureBlobStorage(StorageConfiguration storageConfiguration, AzureBlobStorageConnection azureBlobStorageConnection,
                            String contextPath, String fileType, String mediaType, MediaProcessor mediaProcessor) {

        super(storageConfiguration, azureBlobStorageConnection, contextPath, fileType, mediaType, mediaProcessor);
        this.azureName = azureBlobStorageConnection.getAzureName();
        this.azureKey = azureBlobStorageConnection.getAzureKey();
        this.blobServiceClient = azureBlobStorageConnection.getBlobServiceClient();
    }

    public File getSingleFile() {

        String[] parts = contextPath.split("/", 2);
        String containerName = getContainerName();
        String blobName = getBlobName();
        LOGGER.debug("Blob name: " + blobName);

        InputStream inputStream = ((AzureBlobStorageConnection)storageConnection).loadFile(containerName, blobName);
        return new File(
            inputStream,
            containerName + "/" + blobName,
            blobName);
    }

    @Override
    public FileIterator fileIterator() {
        return new FileIterator();
    }

    public class FileIterator extends BaseStorage.FileIterator {

        @Override
        public boolean hasNext() {
            return getBlobIterator().hasNext();
        }

        @Override
        public File next() {

            BlobItem blobItem = blobIterator.next();
            LOGGER.debug("Blob name: " + blobItem.getName());
            InputStream content;
            try {
                content = ((AzureBlobStorageConnection)storageConnection).loadFile(contextPath, blobItem.getName());

            } catch (Exception e) {
                throw new ModuleException(
                    String.format("Error while parsing document %s.", contextPath),
                    MuleVectorsErrorType.TRANSFORM_DOCUMENT_PARSING_FAILURE,
                    e);
            }
            return new File(
                content,
                contextPath + "/" + blobItem.getName(),
                blobItem.getName());
        }
    }
}
