package org.mule.extension.vectors.internal.extension;

import org.mule.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import org.mule.runtime.module.artifact.api.classloader.MuleArtifactClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LangchainClassLoaderHandler extends MuleArtifactClassLoader {

  private static final Set<String> MANAGED_PACKAGES = new HashSet<>(Arrays.asList(
      "dev.langchain4j.data.document",
      "dev.langchain4j.document.loader"
  ));

  public LangchainClassLoaderHandler(String artifactId,
                                     ArtifactDescriptor artifactDescriptor,
                                     URL[] urls, ClassLoader parent,
                                     ClassLoaderLookupPolicy lookupPolicy) {

    super(artifactId, artifactDescriptor, urls, createSafeClassLoader(parent), lookupPolicy);
  }

  public static ClassLoader createSafeClassLoader(ClassLoader original) {
    return new ClassLoader(original) {
      private final Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

      @Override
      protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Check if package is managed
        boolean isManagedPackage = MANAGED_PACKAGES.stream()
            .anyMatch(name::startsWith);

        if (isManagedPackage) {
          // Synchronize and check for already loaded classes
          synchronized (loadedClasses) {
            Class<?> existingClass = loadedClasses.get(name);
            if (existingClass != null) {
              return existingClass;
            }

            // Load class only once
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
              loadedClass = findClass(name);
            }

            if (loadedClass != null) {
              loadedClasses.put(name, loadedClass);
              if (resolve) {
                resolveClass(loadedClass);
              }
              return loadedClass;
            }
          }
        }

        // Default class loading for non-managed packages
        return super.loadClass(name, resolve);
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
          return super.findClass(name);
        } catch (ClassNotFoundException e) {
          return getParent().loadClass(name);
        }
      }
    };
  }
}
