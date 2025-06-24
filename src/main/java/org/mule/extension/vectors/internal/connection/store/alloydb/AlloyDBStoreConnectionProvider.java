package org.mule.extension.vectors.internal.connection.store.alloydb;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("alloyDB")
@DisplayName("AlloyDB")
@ExternalLib(name = "LangChain4J AlloyDB",
    type=DEPENDENCY,
    description = "LangChain4J AlloyDB",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.community.store.embedding.alloydb.AlloyDBEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-community-alloydb-pg:1.1.0-beta7")
public class AlloyDBStoreConnectionProvider implements
    CachedConnectionProvider<BaseStoreConnection>,  BaseStoreConnectionProvider {
  
  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private AlloyDBStoreConnectionParameters alloyDBStoreConnectionParameters;
  private  AlloyDBStoreConnection alloyDBStoreConnection;
  
  @Override
  public BaseStoreConnection connect() throws ConnectionException {
      return alloyDBStoreConnection;
  }



  @Override
  public void dispose() {
    alloyDBStoreConnection.disconnect();
  }

  @Override
  public void initialise()  {
    alloyDBStoreConnection =
        new AlloyDBStoreConnection(alloyDBStoreConnectionParameters);
    alloyDBStoreConnection.initialise();
  }
}
