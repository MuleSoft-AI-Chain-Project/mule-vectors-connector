package org.mule.extension.vectors.internal.connection.store.pgvector;

import dev.langchain4j.internal.ValidationUtils;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public PGVectorStoreConnection(String host, int port, String database, String userName, String password) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.user = userName;
    this.password = password;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_PGVECTOR;
  }

  @Override
  public void connect() throws ConnectionException {

    try {

      this.dataSource = createDataSource();
      if(dataSource != null) {

        Connection conn = dataSource.getConnection();
        if (conn == null) {

          throw new ConnectionException("Impossible to connect to PGVector. Cannot get a connection from the pool.");
        } else {

          conn.close();
        }
      } else {

        throw new ConnectionException("Impossible to connect to PGVector. Cannot initiate the datasource.");
      }

    } catch (ConnectionException e) {

      throw e;

    } catch (SQLException e) {

      throw new ConnectionException("Impossible to connect to PGVector. SQLException.", e);

    } catch (Exception e) {

      throw new ConnectionException("Impossible to connect to PGVector.", e);
    }
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
  public boolean isValid() {

    try {

      Connection conn = this.dataSource.getConnection();
      if(conn != null) {

        conn.close();
        return true;
      } else {

        return false;
      }

    } catch (Exception e) {

      return false;

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
