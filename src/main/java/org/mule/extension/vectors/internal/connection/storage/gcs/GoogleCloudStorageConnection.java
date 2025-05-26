package org.mule.extension.vectors.internal.connection.storage.gcs;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.channels.Channels;

import static java.lang.String.format;

public class GoogleCloudStorageConnection implements BaseStorageConnection {

    private String projectId;
    private String clientEmail;
    private String clientId;
    private String privateKeyId;
    private String privateKey;
    private Storage storageService;

    public GoogleCloudStorageConnection(String projectId, String clientEmail, String clientId, String privateKeyId, String privateKey) {
        this.projectId = projectId;
        this.clientEmail = clientEmail;
        this.clientId = clientId;
        this.privateKeyId = privateKeyId;
        this.privateKey = privateKey;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPrivateKeyId() {
        return privateKeyId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public Storage getStorageService() {
        return storageService;
    }

    @Override
    public String getStorageType() {
        return Constants.STORAGE_TYPE_GCS;
    }

    private String buildJsonCredentials() {
        return new StringBuilder()
                .append("{")
                .append("\"type\": \"service_account\",")
                .append("\"project_id\": \"").append(this.projectId).append("\",")
                .append("\"private_key_id\": \"").append(this.privateKeyId).append("\",")
                .append("\"private_key\": \"").append(this.privateKey).append("\",")
                .append("\"client_email\": \"").append(this.clientEmail).append("\",")
                .append("\"client_id\": \"").append(this.clientId).append("\",")
                .append("\"auth_uri\": \"").append(Constants.GCP_AUTH_URI).append("\",")
                .append("\"token_uri\": \"").append(Constants.GCP_TOKEN_URI).append("\",")
                .append("\"auth_provider_x509_cert_url\": \"").append(Constants.GCP_AUTH_PROVIDER_X509_CERT_URL).append("\",")
                .append("\"client_x509_cert_url\": \"").append(Constants.GCP_CLIENT_X509_CERT_URL).append(this.clientEmail).append("\",")
                .append("\"universe_domain\": \"googleapis.com\"")
                .append("}")
                .toString();
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            ServiceAccountCredentials
                serviceAccountCredentials = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(buildJsonCredentials().getBytes()));
            this.storageService = StorageOptions.newBuilder()
                    .setCredentials(serviceAccountCredentials)
                    .build()
                    .getService();
        } catch (Exception e) {
            throw new ConnectionException("Failed to connect to Google Cloud Storage.", e);
        }
    }

    @Override
    public void disconnect() {
        if (this.storageService != null) {
            try {
                this.storageService.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isValid() {
        this.storageService.list();
        return true;
    }

    public Document loadDocument(String bucket, String objectName, DocumentParser parser) {
        Blob blob = this.storageService.get(bucket, objectName);
        if (blob == null) {
            throw new IllegalArgumentException("Object gs://" + bucket + "/" + objectName + " couldn't be found.");
        }
        try {

            InputStream inputStream = Channels.newInputStream(blob.reader());
            Document document = parser.parse(inputStream);
            document.metadata().put("source", "gs://" + blob.getBucket() + "/" + blob.getName());
            document.metadata().put("bucket", blob.getBucket());
            document.metadata().put("name", blob.getName());
            document.metadata().put("contentType", blob.getContentType());
            document.metadata().put("size", blob.getSize());
            document.metadata().put("createTime", blob.getCreateTimeOffsetDateTime().toString());
            document.metadata().put("updateTime", blob.getUpdateTimeOffsetDateTime().toString());
            return document;

        } catch (BlankDocumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load document", e);
        }
    }
}
