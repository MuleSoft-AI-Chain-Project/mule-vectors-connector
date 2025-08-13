package org.mule.extension.vectors.internal.connection.storage.gcs;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

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


    public void initialise() throws IOException {

            ServiceAccountCredentials
                serviceAccountCredentials = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(buildJsonCredentials().getBytes()));
            this.storageService = StorageOptions.newBuilder()
                    .setCredentials(serviceAccountCredentials)
                    .build()
                    .getService();

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
    public void validate() {
        this.storageService.list();
    }


}
