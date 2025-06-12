package org.mule.extension.vectors.internal.connection.store.mongodbatlas;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

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
    String database = parameters.getDatabase();
    String options = parameters.getOptions();
    this.mongodbUri =
        (port != null ? "mongodb" : "mongodb+srv") +
            "://" + user + ":" + password + "@" + host +
            (port != null ? ":" + port : "") + "/" +
            (options != null && !options.isEmpty() ? "?" + options : "");
    LOGGER.debug(mongodbUri);
    this.database = database;
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

    if(this.mongoClient != null) {

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
    if (parameters.getHost() == null || parameters.getHost().isBlank()) {
      throw new IllegalArgumentException("Host is required for MongoDB Atlas connection");
    }
    if (parameters.getUser() == null || parameters.getUser().isBlank()) {
      throw new IllegalArgumentException("User is required for MongoDB Atlas connection");
    }
    if (parameters.getPassword() == null || parameters.getPassword().isBlank()) {
      throw new IllegalArgumentException("Password is required for MongoDB Atlas connection");
    }
    if (parameters.getDatabase() == null || parameters.getDatabase().isBlank()) {
      throw new IllegalArgumentException("Database is required for MongoDB Atlas connection");
    }
  }
}
