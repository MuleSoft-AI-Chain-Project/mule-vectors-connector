package org.mule.extension.vectors.internal.connection.store.ephemeralfile;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

public class EphemeralFileStoreConnection implements BaseStoreConnection {

  private String workingDir;
  private final EphemeralFileStoreConnectionParameters parameters;

  public EphemeralFileStoreConnection(EphemeralFileStoreConnectionParameters parameters) {
    this.parameters = parameters;
    this.workingDir = parameters.getWorkingDir();
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_EPHEMERAL_FILE;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    if (parameters.getWorkingDir() == null || parameters.getWorkingDir().isBlank()) {
      throw new IllegalArgumentException("Working directory is required for Ephemeral File connection");
    }
  }
}
