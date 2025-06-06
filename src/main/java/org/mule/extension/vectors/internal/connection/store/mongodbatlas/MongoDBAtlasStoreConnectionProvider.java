package org.mule.extension.vectors.internal.connection.store.mongodbatlas;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("mongoDBAtlas")
@DisplayName("MongoDB Atlas")
@ExternalLib(name = "LangChain4J MongoDB Atlas",
    type=DEPENDENCY,
    description = "LangChain4J MongoDB Atlas",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-mongodb-atlas:1.0.1-beta6")
public class MongoDBAtlasStoreConnectionProvider extends BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private MongoDBAtlasStoreConnectionParameters mongoDBAtlasStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection = new MongoDBAtlasStoreConnection(
          mongoDBAtlasStoreConnectionParameters.getHost(),
          mongoDBAtlasStoreConnectionParameters.getPort(),
          mongoDBAtlasStoreConnectionParameters.getUser(),
          mongoDBAtlasStoreConnectionParameters.getPassword(),
          mongoDBAtlasStoreConnectionParameters.getDatabase(),
          mongoDBAtlasStoreConnectionParameters.getOptions());
      mongoDBAtlasStoreConnection.connect();
      return mongoDBAtlasStoreConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to MongoDB Atlas.", e);
    }
  }

  @Override
  public void disconnect(BaseStoreConnection connection) {

    connection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(BaseStoreConnection connection) {

    try {
      if (connection.isValid()) {
        return ConnectionValidationResult.success();
      } else {
        return ConnectionValidationResult.failure("Failed to validate connection to MongoDB Atlas", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to MongoDB Atlas", e);
    }
  }
}
