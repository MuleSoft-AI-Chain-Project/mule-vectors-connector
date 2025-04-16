package org.mule.extension.vectors.internal.storage.azureblob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.loader.azure.storage.blob.AzureBlobStorageDocumentLoader;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Iterator;

import dev.langchain4j.data.document.Document;
import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.media.Image;
import org.mule.extension.vectors.internal.data.media.Media;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.media.MediaProcessor;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.MetadataUtils;
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

    private AzureBlobStorageDocumentLoader loader;

    private AzureBlobStorageDocumentLoader getLoader() {

        if(this.loader == null) {

            this.loader = new AzureBlobStorageDocumentLoader(getBlobServiceClient());
        }
        return this.loader;
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

    public Document getSingleDocument() {

        String[] parts = contextPath.split("/", 2);
        String containerName = getContainerName();
        String blobName = getBlobName();
        LOGGER.debug("Blob name: " + blobName);
        Document document = getLoader().loadDocument(containerName, blobName, documentParser);
        MetadataUtils.addMetadataToDocument(document, fileType, blobName);
        return document;
    }


    public Media getSingleMedia() {

        String[] parts = contextPath.split("/", 2);
        String containerName = parts[0];
        String blobName = parts[1];

        Media media;

        switch (mediaType) {

            case Constants.MEDIA_TYPE_IMAGE:

                media = Media.fromImage(loadImage(containerName, blobName));
                MetadataUtils.addImageMetadataToMedia(media, mediaType);
                break;

            default:
                throw new IllegalArgumentException("Unsupported Media Type: " + mediaType);
        }
        return media;
    }

    private Image loadImage(String containerName, String blobName) {

        Image image;

        try {

            // Get ContainerClient
            BlobContainerClient blobContainerClient = getBlobServiceClient().getBlobContainerClient(containerName);
            // Get BlobClient
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            // Get Blob properties (to fetch MIME type)
            BlobProperties properties = blobClient.getProperties();
            String mimeType = properties.getContentType();

            // Download blob into a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            String format = mimeType.contains("/") ? mimeType.substring(mimeType.indexOf("/") + 1) : null;
            if(mediaProcessor!= null) imageBytes = mediaProcessor.process(imageBytes, format);
            String base64Data = Base64.getEncoder().encodeToString(imageBytes);

            // Encode only special characters, but keep `/`
            String encodedBlobName = URLEncoder.encode(blobName, "UTF-8")
                .replace("+", "%20") // Fix space encoding
                .replace("%2F", "/"); // Keep `/` in the path

            image = Image.builder()
                .url(String.format("https://%s.blob.core.windows.net/%s/%s", azureName, containerName, encodedBlobName))
                .mimeType(mimeType)
                .base64Data(base64Data)
                .build();

        } catch (Exception ioe) {

            throw new ModuleException(String.format("Impossible to load the image from %s", ""),
                                      MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
                                      ioe);
        }
        return image;
    }

    @Override
    public DocumentIterator documentIterator() {
        return new DocumentIterator();
    }

    @Override
    public MediaIterator mediaIterator() {
        return new MediaIterator();
    }

    public class DocumentIterator extends BaseStorage.DocumentIterator {

        @Override
        public boolean hasNext() {
            return getBlobIterator().hasNext();
        }

        @Override
        public Document next() {

            BlobItem blobItem = blobIterator.next();
            LOGGER.debug("Blob name: " + blobItem.getName());
            Document document;
            try {
                document = getLoader().loadDocument(contextPath, blobItem.getName(), documentParser);
            } catch(BlankDocumentException bde) {

                LOGGER.warn(String.format("BlankDocumentException: Error while parsing document %s.", contextPath));
                throw bde;
            } catch (Exception e) {
                throw new ModuleException(
                    String.format("Error while parsing document %s.", contextPath),
                    MuleVectorsErrorType.DOCUMENT_PARSING_FAILURE,
                    e);
            }
            MetadataUtils.addMetadataToDocument(document, fileType, blobItem.getName());
            return document;
        }
    }

    public class MediaIterator extends BaseStorage.MediaIterator {

        @Override
        public boolean hasNext() {
            return getBlobIterator().hasNext();
        }

        @Override
        public Media next() {

            BlobItem blobItem = blobIterator.next();
            LOGGER.debug("Blob name: " + blobItem.getName());
            Media media;
            try {

                media = Media.fromImage(loadImage(contextPath, blobItem.getName()));
                MetadataUtils.addImageMetadataToMedia(media, mediaType);

            } catch (Exception e) {
                throw new ModuleException(
                    String.format("Error while loading media %s.", contextPath),
                    MuleVectorsErrorType.MEDIA_OPERATIONS_FAILURE,
                    e);
            }
            return media;
        }
    }
}
