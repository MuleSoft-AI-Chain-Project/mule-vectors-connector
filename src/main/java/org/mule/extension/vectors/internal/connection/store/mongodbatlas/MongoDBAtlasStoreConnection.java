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

public class MongoDBAtlasStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBAtlasStoreConnection.class);

  private MongoClient mongoClient;
  private String mongodbUri;
  private String database;

  public MongoDBAtlasStoreConnection(String host, Integer port, String user, String password, String database, String options) {

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
  public void connect() {

    this.mongoClient = MongoClients.create(mongodbUri);
    mongoClient.listDatabaseNames().first();
  }

  @Override
  public void disconnect() {

    if(this.mongoClient != null) {

      this.mongoClient.close();
    }
  }

  @Override
  public boolean isValid() {

    try {
      mongoClient.listDatabaseNames().first();
      return true; // Connection is valid
    } catch (MongoException e) {
      // Connection failed or is invalid
      return false;
    }
  }
}
