package org.mule.extension.vectors.internal.extension;

import org.mule.extension.vectors.internal.config.DocumentConfiguration;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
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
@Configurations({DocumentConfiguration.class, EmbeddingConfiguration.class, StoreConfiguration.class})
@RequiresEnterpriseLicense(allowEvaluationLicense = true)
@ErrorTypes(MuleVectorsErrorType.class)
@JavaVersionSupport({JAVA_17})
public class Connector {

}
