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

  public AlloyDBStoreConnection(String projectId, String region, String cluster, String instance,
      String iamAccountEmail, String host, String ipType, int port, String database,
      String userName, @Password String password) {

    this.projectId = projectId;
    this.region = region;
    this.cluster = cluster;
    this.instance = instance;
    this.iamAccountEmail = iamAccountEmail;
    this.host = host;
    this.ipType = ipType;
    this.port = port;
    this.database = database;
    this.user = userName;
    this.password = password;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_ALLOYDB;
  }

@Override
  public void connect() throws ConnectionException {

    try {

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

    } catch (Exception e) {

      throw new ConnectionException("Impossible to connect to AlloyDB.", e);
    }
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
  public boolean isValid() {

    try {

      Connection conn = this.alloyDBEngine.getConnection();
      if(conn != null) {

        return conn.isValid(30000);
      } else {

        return false;
      }

    } catch (Exception e) {

      return false;

    }
  }

  public AlloyDBEngine getAlloyDBEngine() {
    return alloyDBEngine;
  }
}
