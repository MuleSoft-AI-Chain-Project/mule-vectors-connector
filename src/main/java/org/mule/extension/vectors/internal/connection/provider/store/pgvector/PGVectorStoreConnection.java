package org.mule.extension.vectors.internal.connection.provider.store.pgvector;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.error.exception.VectorsRuntimeException;
import org.mule.extension.vectors.internal.helper.validation.ConnectionValidationStrategies;
import org.mule.extension.vectors.internal.util.FipsUtils;
import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.sql.SQLException;

import javax.sql.DataSource;

import dev.langchain4j.internal.ValidationUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    if (FipsUtils.isFipsEnabled()) {
      LOGGER.error("Vector store is not allowed in FIPS enabled environments.");
      throw new VectorsRuntimeException(I18nMessageFactory
          .createStaticMessage("This vector store is not FIPS compliant and cannot be used in FIPS configured environments"));
    }
    this.parameters = parameters;
    this.host = parameters.getHost();
    this.port = parameters.getPort();
    this.database = parameters.getDatabase();
    this.user = parameters.getUser();
    this.password = parameters.getPassword();
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
   * Changed from isValid() to validate() for MuleSoft Connector compliance. Now checks for required parameters.
   */
  @Override
  public void validate() {
    ConnectionValidationStrategies.validatePGVector(parameters);
    try {
      this.dataSource.getConnection();
    } catch (SQLException e) {
      throw new ModuleException("Failed to connect to PG Vector", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private void createDataSource() {

    host = ValidationUtils.ensureNotBlank(host, "host");
    port = ValidationUtils.ensureGreaterThanZero(port, "port");
    user = ValidationUtils.ensureNotBlank(user, "user");
    password = ValidationUtils.ensureNotBlank(password, "password");
    database = ValidationUtils.ensureNotBlank(database, "database");
    PGSimpleDataSource source = new PGSimpleDataSource();
    source.setServerNames(new String[] {host});
    source.setPortNumbers(new int[] {port});
    source.setDatabaseName(database);
    source.setUser(user);
    source.setPassword(password);
    this.dataSource = source;
  }


  public void dispose() throws SQLException {
    this.dataSource.getConnection().close();
  }

  public void initialise() {
    createDataSource();
  }
}
