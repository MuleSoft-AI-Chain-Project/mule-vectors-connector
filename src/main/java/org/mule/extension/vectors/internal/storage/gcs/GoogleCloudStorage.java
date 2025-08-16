package org.mule.extension.vectors.internal.storage.gcs;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.gcs.GoogleCloudStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;

import java.io.InputStream;
import java.nio.channels.Channels;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCloudStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorage.class);

  private final Storage storageService;
  Blob blob;

  public GoogleCloudStorage(StorageConfiguration storageConfiguration,
                            GoogleCloudStorageConnection googleCloudStorageConnection) {
    this.storageService = googleCloudStorageConnection.getStorageService();
  }

  public Blob getBlob() {
    return blob;
  }

  public InputStream loadFile(String bucket, String objectName) {
    blob = this.storageService.get(bucket, objectName);
    if (blob == null) {
      throw new IllegalArgumentException("Object gs://" + bucket + "/" + objectName + " couldn't be found.");
    }
    try {
      return Channels.newInputStream(blob.reader());
    } catch (Exception e) {
      throw new RuntimeException("Failed to load document", e);
    }
  }

  public static String[] parseContextPath(String contextPath) {
    if (!contextPath.toLowerCase().startsWith(Constants.GCS_PREFIX)) {
      throw new IllegalArgumentException(String.format(
                                                       "Invalid GCS path: '%s'. Path must start with '%s' and contain both bucket and object key.",
                                                       contextPath, Constants.GCS_PREFIX));
    }
    String pathWithoutPrefix = contextPath.substring(Constants.GCS_PREFIX.length());
    int firstSlashIndex = pathWithoutPrefix.indexOf('/');
    String bucket;
    String objectKey = "";
    if (firstSlashIndex == -1) {
      bucket = pathWithoutPrefix;
    } else {
      bucket = pathWithoutPrefix.substring(0, firstSlashIndex);
      objectKey = pathWithoutPrefix.substring(firstSlashIndex + 1);
    }
    return new String[] {bucket, objectKey};
  }

  public Storage getStorageService() {
    return storageService;
  }
}
