package org.mule.extension.vectors.internal.connection.store.alloydb;

import java.sql.Connection;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.store.alloydb.CustomAlloyDBEngine;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

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
    if (parameters.getProjectId() == null ) {
      throw new IllegalArgumentException("Project ID is required for AlloyDB connection");
    }
    if (parameters.getRegion() == null ) {
      throw new IllegalArgumentException("Region is required for AlloyDB connection");
    }
    if (parameters.getCluster() == null) {
      throw new IllegalArgumentException("Cluster is required for AlloyDB connection");
    }
    if (parameters.getInstance() == null ) {
      throw new IllegalArgumentException("Instance is required for AlloyDB connection");
    }
    if (parameters.getIamAccountEmail() == null ) {
      throw new IllegalArgumentException("IAM Account Email is required for AlloyDB connection");
    }
    if (parameters.getHost() == null ) {
      throw new IllegalArgumentException("Host is required for AlloyDB connection");
    }
    if (parameters.getPort() <= 0) {
      throw new IllegalArgumentException("Port is required for AlloyDB connection and must be > 0");
    }
    if (parameters.getDatabase() == null ) {
      throw new IllegalArgumentException("Database is required for AlloyDB connection");
    }
    if (parameters.getUser() == null ) {
      throw new IllegalArgumentException("User is required for AlloyDB connection");
    }
    if (parameters.getPassword() == null) {
      throw new IllegalArgumentException("Password is required for AlloyDB connection");
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
