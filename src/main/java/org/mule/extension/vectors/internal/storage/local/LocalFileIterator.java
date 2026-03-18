package org.mule.extension.vectors.internal.storage.local;

import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileIterator implements FileIterator, AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileIterator.class);

  private final LocalStorage localClient;
  private final String fullPath;
  private Iterator<Path> pathIterator;
  private DirectoryStream<Path> directoryStream;

  public LocalFileIterator(LocalStorage localClient, String directory) {
    this.localClient = localClient;
    this.fullPath = directory;
    this.pathIterator = null;
    this.directoryStream = null;
  }

  private void fetchNextPathIterator() {
    try {
      directoryStream = Files.newDirectoryStream(Paths.get(fullPath), Files::isRegularFile);
      pathIterator = directoryStream.iterator();
    } catch (IOException e) {
      LOGGER.error("Failed to open directory stream for path: {}", fullPath, e);
      pathIterator = Collections.emptyIterator();
    }
  }

  private Iterator<Path> getPathIterator() {
    if (pathIterator == null) {
      fetchNextPathIterator();
    }
    return pathIterator;
  }

  @Override
  public boolean hasNext() {
    return getPathIterator().hasNext();
  }

  @Override
  public FileInfo next() {
    if (!hasNext())
      throw new NoSuchElementException();
    Path path = getPathIterator().next();
    InputStream content = localClient.loadFile(path);
    String mimeType = null;
    try {
      mimeType = Files.probeContentType(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      // Fallback if mimeType is null
      if (mimeType == null) {
        mimeType = Utils.getMimeTypeFallback(path);
      }
    }
    return new FileInfo(content, path.toString(), LocalStorage.parseFileName(path.toString()), mimeType);
  }

  @Override
  public void close() {
    if (directoryStream != null) {
      try {
        directoryStream.close();
      } catch (IOException e) {
        LOGGER.error("Failed to close directory stream for path: {}", fullPath, e);
      }
    }
  }
}
