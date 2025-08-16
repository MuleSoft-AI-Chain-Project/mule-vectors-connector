package org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.validation.ConnectionValidationStrategies;
import org.mule.runtime.extension.api.exception.ModuleException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBAtlasStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBAtlasStoreConnection.class);

  private MongoClient mongoClient;
  private String mongodbUri;
  private String database;
  private final MongoDBAtlasStoreConnectionParameters parameters;

  public MongoDBAtlasStoreConnection(MongoDBAtlasStoreConnectionParameters parameters) {
    this.parameters = parameters;
    String host = parameters.getHost();
    Integer port = parameters.getPort();
    String user = parameters.getUser();
    String password = parameters.getPassword();
    String databaseName = parameters.getDatabase();
    String options = parameters.getOptions();
    this.mongodbUri =
        (port != null ? "mongodb" : "mongodb+srv") +
            "://" + user + ":" + password + "@" + host +
            (port != null ? ":" + port : "") + "/" +
            (options != null && !options.isEmpty() ? "?" + options : "");
    LOGGER.debug(mongodbUri);
    this.database = databaseName;
  }

  public MongoClient getMongoClient() {
    return mongoClient;
  }

  public String getDatabase() {
    return database;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_MONGODB_ATLAS;
  }

  @Override
  public void disconnect() {

    if (this.mongoClient != null) {

      this.mongoClient.close();
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
    ConnectionValidationStrategies.validateMongoDBAtlas(parameters);
    try {
      mongoClient.listDatabaseNames().first();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to MongoDB Atlas store", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  public void initialise() {

    this.mongoClient = MongoClients.create(mongodbUri);
    mongoClient.listDatabaseNames().first();
  }

}
