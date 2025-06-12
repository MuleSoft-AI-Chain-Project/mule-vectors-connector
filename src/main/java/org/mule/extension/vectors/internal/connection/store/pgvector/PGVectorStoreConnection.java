package org.mule.extension.vectors.internal.connection.store.pgvector;

import dev.langchain4j.internal.ValidationUtils;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.connection.store.pgvector.PGVectorStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PGVectorStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(PGVectorStoreConnection.class);

  private String host;
  private int port;
  private String database;
  private String user;
  private String password;
  private DataSource dataSource;
  private final PGVectorStoreConnectionParameters parameters;

  public PGVectorStoreConnection(PGVectorStoreConnectionParameters parameters) {
    this.parameters = parameters;
    this.host = parameters.getHost();
    this.port = parameters.getPort();
    this.database = parameters.getDatabase();
    this.user = parameters.getUser();
    this.password = parameters.getPassword();
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getDatabase() {
    return database;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_PGVECTOR;
  }

  @Override
  public void disconnect() {

    try {

      this.dataSource.getConnection().close();
    } catch (SQLException e) {

      LOGGER.error("Unable to close the connection to PGVector.", e);

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
    if (parameters.getHost() == null || parameters.getHost().isBlank()) {
      throw new IllegalArgumentException("Host is required for PGVector connection");
    }
    if (parameters.getPort() <= 0) {
      throw new IllegalArgumentException("Port is required for PGVector connection and must be > 0");
    }
    if (parameters.getDatabase() == null || parameters.getDatabase().isBlank()) {
      throw new IllegalArgumentException("Database is required for PGVector connection");
    }
    if (parameters.getUser() == null || parameters.getUser().isBlank()) {
      throw new IllegalArgumentException("User is required for PGVector connection");
    }
    if (parameters.getPassword() == null || parameters.getPassword().isBlank()) {
      throw new IllegalArgumentException("Password is required for PGVector connection");
    }
  }

  private DataSource createDataSource() {

    host = ValidationUtils.ensureNotBlank(host, "host");
    port = ValidationUtils.ensureGreaterThanZero(port, "port");
    user = ValidationUtils.ensureNotBlank(user, "user");
    password = ValidationUtils.ensureNotBlank(password, "password");
    database = ValidationUtils.ensureNotBlank(database, "database");
    PGSimpleDataSource source = new PGSimpleDataSource();
    source.setServerNames(new String[]{host});
    source.setPortNumbers(new int[]{port});
    source.setDatabaseName(database);
    source.setUser(user);
    source.setPassword(password);
    return source;
  }
}
