package org.mule.extension.vectors.internal.extension;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.ImageProcessorParameters;
import org.mule.extension.vectors.internal.helper.parameter.MediaProcessorParameters;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.sdk.api.annotation.JavaVersionSupport;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_17;

/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "ms-vectors")
@Extension(name = "MuleSoft Vectors Connector", category = Category.SELECT)
@Configurations({StorageConfiguration.class, EmbeddingConfiguration.class, StoreConfiguration.class})
@ErrorTypes(MuleVectorsErrorType.class)
@JavaVersionSupport({JAVA_17})
@SubTypeMapping(baseType = MediaProcessorParameters.class,
    subTypes = {ImageProcessorParameters.class})
public class Connector {

}
