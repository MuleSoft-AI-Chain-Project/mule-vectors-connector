package org.mule.extension.vectors.internal.connection.provider.store.milvus;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("milvus")
@DisplayName("Milvus")
@ExternalLib(name = "LangChain4J Milvus",
    type = DEPENDENCY,
    description = "LangChain4J Milvus",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-milvus:1.8.0-beta15")
public class MilvusStoreConnectionProvider implements BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private MilvusStoreConnectionParameters milvusStoreConnectionParameters;
  private MilvusStoreConnection milvusStoreConnection;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {
    return milvusStoreConnection;
  }

  @Override
  public void dispose() {
    milvusStoreConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
    milvusStoreConnection = new MilvusStoreConnection(
                                                      milvusStoreConnectionParameters);
    milvusStoreConnection.initialise();
  }
}
