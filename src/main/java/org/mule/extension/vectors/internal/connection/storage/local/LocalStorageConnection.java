package org.mule.extension.vectors.internal.connection.storage.local;

import static java.lang.String.format;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.notExists;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalStorageConnection implements BaseStorageConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageConnection.class);

  private String workingDir;

  public LocalStorageConnection(String workingDir) {
    this.workingDir = workingDir;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  @Override
  public String getStorageType() {
    return Constants.STORAGE_TYPE_LOCAL;
  }


  public void initialise() {
    try {
      validateWorkingDir();
    } catch (ConnectionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void disconnect() {
    // no-op
  }

  @Override
  public void validate() {
    try {
      validateWorkingDir();
    } catch (ConnectionException e) {
      //      throw new RuntimeException(e);
    }
  }

  private void validateWorkingDir() throws ConnectionException {

    if (workingDir == null) {
      workingDir = System.getProperty("user.home");
      if (workingDir == null) {
        throw new ConnectionException("Could not obtain user's home directory. Please provide a explicit value for the workingDir parameter");
      }

      LOGGER.warn("File connector does not specify the workingDir property. Defaulting to '{}'", workingDir);
    }

    Path workingDirPath = Paths.get(workingDir);
    if (notExists(workingDirPath)) {
      throw new ConnectionException(format("Provided workingDir '%s' does not exists", workingDirPath.toAbsolutePath()));
    }

    if (!isDirectory(workingDirPath)) {
      throw new ConnectionException(format("Provided workingDir '%s' is not a directory", workingDirPath.toAbsolutePath()));
    }
  }


}
