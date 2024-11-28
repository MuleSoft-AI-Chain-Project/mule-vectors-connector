package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.extension.vectors.internal.operation.DocumentOperations;
import org.mule.extension.vectors.internal.operation.EmbeddingOperations;
import org.mule.extension.vectors.internal.storage.BaseStorageConfiguration;
import org.mule.extension.vectors.internal.store.BaseStoreConfiguration;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@org.mule.runtime.extension.api.annotation.Configuration(name = "config")
@Operations({EmbeddingOperations.class, DocumentOperations.class})
public class Configuration {

  @Parameter
  @Alias("vectorStore")
  @DisplayName("Vector Store")
  @Summary("The vector store.")
  @Placement(order = 1, tab = Placement.DEFAULT_TAB)
  private BaseStoreConfiguration storeConfiguration;

  @Parameter
  @Alias("storage")
  @DisplayName("Storage")
  @Summary("The storage type.")
  @Optional
  @Placement(order = 2, tab = Placement.DEFAULT_TAB)
  private BaseStorageConfiguration storageConfiguration;

  public BaseStoreConfiguration getStoreConfiguration() {
    return storeConfiguration;
  }

  public BaseStorageConfiguration getStorageConfiguration() {
    return storageConfiguration;
  }
}
