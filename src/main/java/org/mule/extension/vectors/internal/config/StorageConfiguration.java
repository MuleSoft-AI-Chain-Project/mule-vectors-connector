package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.connection.storage.amazons3.AmazonS3StorageConnectionProvider;
import org.mule.extension.vectors.internal.connection.storage.azureblob.AzureBlobStorageConnectionProvider;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnectionProvider;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnectionProvider;
import org.mule.extension.vectors.internal.operation.StorageOperations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@org.mule.runtime.extension.api.annotation.Configuration(name = "storageConfig")
@ConnectionProviders({
    AmazonS3StorageConnectionProvider.class,
    AzureBlobStorageConnectionProvider.class,
    GoogleCloudStorageConnectionProvider.class,
    LocalStorageConnectionProvider.class})
@Operations({StorageOperations.class})
public class StorageConfiguration {

}
