package org.mule.extension.vectors.internal.connection.store.ephemeralfile;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EphemeralFileStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(EphemeralFileStoreConnection.class);

  private String workingDir;

  public EphemeralFileStoreConnection(String workingDir) {
    this.workingDir = workingDir;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_EPHEMERAL_FILE;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  @Override
  public void connect() throws ConnectionException {

  }

  @Override
  public void disconnect() {

  }

  @Override
  public boolean isValid() {

    try {

      return true;

    } catch (Exception e) {

      LOGGER.error("Failed to validate connection to Ephemeral File.", e);
      return false;
    }
  }
}
