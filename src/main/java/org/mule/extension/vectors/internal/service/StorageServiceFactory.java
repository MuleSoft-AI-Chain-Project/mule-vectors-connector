package org.mule.extension.vectors.internal.service;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnection;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import org.mule.extension.vectors.internal.service.storage.S3StorageService;
import org.mule.extension.vectors.internal.service.storage.AzureBlobStorageService;
import org.mule.extension.vectors.internal.service.storage.GoogleCloudStorageService;
import org.mule.extension.vectors.internal.service.storage.LocalStorageService;
import org.mule.extension.vectors.internal.service.storage.StorageService;
import org.mule.extension.vectors.internal.storage.amazons3.AmazonS3Storage;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorage;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorage;
import org.mule.extension.vectors.internal.storage.local.LocalStorage;

public class StorageServiceFactory {
    public static StorageService getService(StorageConfiguration config, BaseStorageConnection connection) {
        if (connection instanceof AmazonS3StorageConnection) {
            AmazonS3Storage s3Client = new AmazonS3Storage(config, (AmazonS3StorageConnection) connection);
            return new S3StorageService(s3Client);
        }
        if (connection instanceof AzureBlobStorageConnection) {
            AzureBlobStorage azureClient = new AzureBlobStorage(config, (AzureBlobStorageConnection) connection);
            return new AzureBlobStorageService(azureClient);
        }
        if (connection instanceof GoogleCloudStorageConnection) {
            GoogleCloudStorage gcsClient = new GoogleCloudStorage(config, (GoogleCloudStorageConnection) connection);
            return new GoogleCloudStorageService(gcsClient);
        }
        if (connection instanceof LocalStorageConnection) {
            LocalStorage localClient = new LocalStorage(config, (LocalStorageConnection) connection);
            return new LocalStorageService(localClient);
        }
        throw new IllegalArgumentException("Unsupported storage type");
    }
} 
