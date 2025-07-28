package org.mule.extension.vectors.internal.connection.provider.store.alloydb;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.validation.ConnectionValidationStrategies;
import org.mule.extension.vectors.internal.service.store.alloydb.CustomAlloyDBEngine;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.sql.SQLException;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlloyDBStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlloyDBStoreConnection.class);

  private String projectId;
  private String region;
  private String cluster;
  private String instance;
  private String iamAccountEmail;
  private String host;
  private String ipType;
  private int port;
  private String database;
  private String user;
  private String password;

  private AlloyDBEngine alloyDBEngine;
  private final AlloyDBStoreConnectionParameters parameters;

  public AlloyDBStoreConnection(AlloyDBStoreConnectionParameters parameters) {
    this.parameters = parameters;
    this.projectId = parameters.getProjectId();
    this.region = parameters.getRegion();
    this.cluster = parameters.getCluster();
    this.instance = parameters.getInstance();
    this.iamAccountEmail = parameters.getIamAccountEmail();
    this.host = parameters.getHost();
    this.ipType = parameters.getIpType();
    this.port = parameters.getPort();
    this.database = parameters.getDatabase();
    this.user = parameters.getUser();
    this.password = parameters.getPassword();
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_ALLOYDB;
  }

  @Override
  public void disconnect() {

    try {

      this.alloyDBEngine.close();
    } catch (Exception e) {

      LOGGER.error("Unable to close the connection to AlloyDB.", e);

    }
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
    ConnectionValidationStrategies.validateAlloyDB(parameters);
    try {
      this.alloyDBEngine.getConnection();
    } catch (SQLException e) {
      throw new ModuleException("Failed to connect to Alloy DB", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  public AlloyDBEngine getAlloyDBEngine() {
    return alloyDBEngine;
  }

  public void initialise() {


    this.alloyDBEngine = new CustomAlloyDBEngine.Builder()
        .projectId(projectId)
        .region(region)
        .cluster(cluster)
        .instance(instance)
        .iamAccountEmail(iamAccountEmail)
        .host(host)
        .ipType(ipType)
        .port(port)
        .database(database)
        .user(user)
        .password(password)
        .build();


  }
}
