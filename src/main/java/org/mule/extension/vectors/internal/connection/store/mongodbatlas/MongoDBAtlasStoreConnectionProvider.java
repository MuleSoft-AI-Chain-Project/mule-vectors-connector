package org.mule.extension.vectors.internal.connection.store.mongodbatlas;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
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
public class MongoDBAtlasStoreConnectionProvider implements
    CachedConnectionProvider<BaseStoreConnection>, BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private MongoDBAtlasStoreConnectionParameters mongoDBAtlasStoreConnectionParameters;
  private   MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection;
  @Override
  public BaseStoreConnection connect() throws ConnectionException {
      return mongoDBAtlasStoreConnection;
  }

  @Override
  public void dispose() {
    mongoDBAtlasStoreConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
     mongoDBAtlasStoreConnection = new MongoDBAtlasStoreConnection(mongoDBAtlasStoreConnectionParameters);
    mongoDBAtlasStoreConnection.initialise();
  }
}
