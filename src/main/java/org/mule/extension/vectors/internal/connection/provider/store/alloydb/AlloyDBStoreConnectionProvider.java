package org.mule.extension.vectors.internal.connection.provider.store.alloydb;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("alloyDB")
@DisplayName("AlloyDB")
@ExternalLib(name = "LangChain4J AlloyDB",
    type = DEPENDENCY,
    description = "LangChain4J AlloyDB",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.community.store.embedding.alloydb.AlloyDBEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-community-alloydb-pg:1.8.0-beta15")
public class AlloyDBStoreConnectionProvider implements BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private AlloyDBStoreConnectionParameters alloyDBStoreConnectionParameters;
  private AlloyDBStoreConnection alloyDBStoreConnection;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {
    return alloyDBStoreConnection;
  }



  @Override
  public void dispose() {
    alloyDBStoreConnection.disconnect();
  }

  @Override
  public void initialise() {
    alloyDBStoreConnection =
        new AlloyDBStoreConnection(alloyDBStoreConnectionParameters);
    alloyDBStoreConnection.initialise();
  }
}
