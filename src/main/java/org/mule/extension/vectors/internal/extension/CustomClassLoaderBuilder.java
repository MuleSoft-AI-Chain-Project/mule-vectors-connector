package org.mule.extension.vectors.internal.extension;


import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import org.mule.runtime.module.artifact.api.classloader.MuleArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.MuleDeployableArtifactClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.net.URL;
import java.util.List;

import static org.mule.extension.vectors.internal.extension.LangchainClassLoaderHandler.createSafeClassLoader;

public class CustomClassLoaderBuilder extends MuleDeployableArtifactClassLoader {

  public CustomClassLoaderBuilder(String artifactId, ArtifactDescriptor artifactDescriptor, URL[] urls, ClassLoader parent,
                                  ClassLoaderLookupPolicy lookupPolicy,
                                  List<ArtifactClassLoader> artifactPluginClassLoaders) {

    super(artifactId, artifactDescriptor, urls, createSafeClassLoader(parent), lookupPolicy, artifactPluginClassLoaders);
  }
}
