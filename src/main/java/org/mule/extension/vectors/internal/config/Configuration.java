package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.model.BaseModelConfiguration;
import org.mule.extension.vectors.internal.operation.DocumentOperations;
import org.mule.extension.vectors.internal.operation.EmbeddingOperations;
import org.mule.extension.vectors.internal.storage.BaseStorageConfiguration;
import org.mule.extension.vectors.internal.store.BaseStoreConfiguration;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.meta.ExternalLibraryType;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@org.mule.runtime.extension.api.annotation.Configuration(name = "config")
@Operations({EmbeddingOperations.class, DocumentOperations.class})

@ExternalLib(name = "Vector Store",
    description = "The vector store dependency to enable support for one of the available stores.",
    type = ExternalLibraryType.DEPENDENCY,
    coordinates = "dev.langchain4j:langchain4j-(qdrant|milvus|chroma|elasticsearch|opensearch|pgvector|pinecone|azure-ai-search):" + Constants.LANG_CHAIN_4J_VERSION,
    optional=true)

@ExternalLib(name = "Embedding Model Service",
    description = "The embedding model service dependency to enable support for one of the available models.",
    type = ExternalLibraryType.DEPENDENCY,
    coordinates = "dev.langchain4j:langchain4j-(azure-open-ai|open-ai|mistral-ai|nomic|hugging-face):" + Constants.LANG_CHAIN_4J_VERSION,
    optional=true)

@ExternalLib(name = "Storage",
    description = "The storage dependency to enable Amazon S3 (langchain4j-document-loader-amazon-s3) and " +
        "Azure Blob Storage (langchain4j-document-loader-azure-storage-blob) support. You can add multiple storage " +
        "dependencies to your pom.xml file if you want to use more than one.",
    type = ExternalLibraryType.DEPENDENCY,
    coordinates = "dev.langchain4j:langchain4j-document-loader-(amazon-s3|azure-storage-blob):" + Constants.LANG_CHAIN_4J_VERSION,
    optional=true)

public class Configuration {

  @Parameter
  @Alias("embeddingModelService")
  @DisplayName("Embedding Model Service")
  @Summary("The embedding model service.")
  @Placement(order = 1, tab = Placement.DEFAULT_TAB)
  private BaseModelConfiguration modelConfiguration;

  @Parameter
  @Alias("vectorStore")
  @DisplayName("Vector Store")
  @Summary("The vector store.")
  @Placement(order = 2, tab = Placement.DEFAULT_TAB)
  private BaseStoreConfiguration storeConfiguration;

  @Parameter
  @Alias("storage")
  @DisplayName("Storage")
  @Summary("The storage type.")
  @Optional
  @Placement(order = 3, tab = Placement.DEFAULT_TAB)
  private BaseStorageConfiguration storageConfiguration;

  public BaseModelConfiguration getModelConfiguration() {
    return modelConfiguration;
  }

  public BaseStoreConfiguration getStoreConfiguration() {
    return storeConfiguration;
  }

  public BaseStorageConfiguration getStorageConfiguration() {
    return storageConfiguration;
  }
}
