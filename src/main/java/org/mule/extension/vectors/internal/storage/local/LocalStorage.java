package org.mule.extension.vectors.internal.storage.local;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorage extends BaseStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorage.class);

  private final String fullPath;

  private List<Path> pathList;
  private Iterator<Path> pathIterator;

  public LocalStorage(StorageConfiguration storageConfiguration, LocalStorageConnection storageConnection, String contextPath) {

    super(storageConfiguration, storageConnection, contextPath);
    this.fullPath = storageConnection.getWorkingDir() != null ? storageConnection.getWorkingDir() + "/" + contextPath : contextPath;
  }

  public File getSingleFile() {

    InputStream inputStream = ((LocalStorageConnection)storageConnection).loadFile(Paths.get(fullPath));
    return new File(
        inputStream,
        fullPath,
        Utils.getFileNameFromPath(fullPath));
  }

  @Override
  public FileIterator fileIterator() {
    return new FileIterator();
  }

  private Iterator<Path> getPathIterator() {
    if (pathList == null) {  // Only load files if not already loaded
      try (Stream<Path> paths = Files.walk(Paths.get(fullPath))) {
        // Collect all files as a list
        pathList = paths.filter(Files::isRegularFile).collect(Collectors.toList());
        // Create an iterator for the list of files
        pathIterator = pathList.iterator();
      } catch (IOException e) {
        throw new ModuleException(
            String.format("Error while getting document from %s.", fullPath),
            MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
            e);
      }
    }
    return pathIterator;
  }

  public class FileIterator extends BaseStorage.FileIterator {

    // Override hasNext to check if there are files left to process
    @Override
    public boolean hasNext() {
      return getPathIterator() != null && getPathIterator().hasNext();
    }

    // Override next to return the next document
    @Override
    public File next() {
      if (hasNext()) {
        Path path = getPathIterator().next();
        LOGGER.debug("File: " + path.getFileName().toString());
        InputStream content;
        try {
          content = ((LocalStorageConnection)storageConnection).loadFile(path);
        } catch (Exception e) {

          throw new ModuleException(
              String.format("Error while loading file %s.", path.toString()),
              MuleVectorsErrorType.STORAGE_OPERATIONS_FAILURE,
              e);
        }
        return new File(
            content,
            path.toString(),
            Utils.getFileNameFromPath(path.toString()));
      }
      throw new IllegalStateException("No more files to iterate");
    }
  }
}
