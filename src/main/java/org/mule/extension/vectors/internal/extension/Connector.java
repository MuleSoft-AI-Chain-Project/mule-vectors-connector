package org.mule.extension.vectors.internal.extension;

import org.mule.extension.vectors.internal.config.Configuration;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.extension.vectors.internal.model.BaseModelConnectionProvider;
import org.mule.extension.vectors.internal.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.model.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.storage.BaseStorageConfiguration;
import org.mule.extension.vectors.internal.storage.azureblob.AzureBlobStorageConfiguration;
import org.mule.extension.vectors.internal.storage.gcs.GoogleCloudStorageConfiguration;
import org.mule.extension.vectors.internal.storage.local.LocalStorageConfiguration;
import org.mule.extension.vectors.internal.storage.s3.AWSS3StorageConfiguration;
import org.mule.extension.vectors.internal.store.BaseStoreConfiguration;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStoreConfiguration;
import org.mule.extension.vectors.internal.store.chroma.ChromaStoreConfiguration;
import org.mule.extension.vectors.internal.store.elasticsearch.ElasticsearchStoreConfiguration;
import org.mule.extension.vectors.internal.store.milvus.MilvusStoreConfiguration;
import org.mule.extension.vectors.internal.store.opensearch.OpenSearchStoreConfiguration;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStoreConfiguration;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStoreConfiguration;
import org.mule.extension.vectors.internal.store.qdrant.QdrantStoreConfiguration;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;
import org.mule.sdk.api.annotation.JavaVersionSupport;

import static org.mule.sdk.api.meta.JavaVersion.JAVA_11;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_17;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_8;

/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "ms-vectors")
@Extension(name = "MuleSoft Vectors Connector", category = Category.SELECT)
@Configurations(Configuration.class)
@SubTypeMapping(baseType = BaseStoreConfiguration.class,
    subTypes = {
        AISearchStoreConfiguration.class,
        ChromaStoreConfiguration.class,
        ElasticsearchStoreConfiguration.class,
        MilvusStoreConfiguration.class,
        OpenSearchStoreConfiguration.class,
        PGVectorStoreConfiguration.class,
        PineconeStoreConfiguration.class,
        QdrantStoreConfiguration.class})
@SubTypeMapping(baseType = BaseModelConnection.class,
    subTypes = {
        AzureOpenAIModelConnection.class,
        EinsteinModelConnection.class,
        HuggingFaceModelConnection.class,
        MistralAIModelConnection.class,
        NomicModelConnection.class,
        OpenAIModelConnection.class})
@SubTypeMapping(baseType = BaseStorageConfiguration.class,
    subTypes = {
        LocalStorageConfiguration.class,
        AWSS3StorageConfiguration.class,
        AzureBlobStorageConfiguration.class,
        GoogleCloudStorageConfiguration.class})
@ConnectionProviders(BaseModelConnectionProvider.class)
@RequiresEnterpriseLicense(allowEvaluationLicense = true)
@ErrorTypes(MuleVectorsErrorType.class)
@JavaVersionSupport({JAVA_8, JAVA_11, JAVA_17})
public class Connector {

}
