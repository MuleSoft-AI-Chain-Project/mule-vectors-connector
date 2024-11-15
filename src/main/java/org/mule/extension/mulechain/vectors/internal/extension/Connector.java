package org.mule.extension.mulechain.vectors.internal.extension;

import org.mule.extension.mulechain.vectors.internal.config.Configuration;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.sdk.api.annotation.JavaVersionSupport;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_11;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_17;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_8;

/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "vectors")
@Extension(name = "MAC Vectors")
@Configurations(Configuration.class)
@JavaVersionSupport({JAVA_8, JAVA_11, JAVA_17})
public class Connector {

}