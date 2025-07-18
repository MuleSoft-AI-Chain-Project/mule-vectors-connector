package org.mule.extension.vectors.internal.connection.provider.store.pinecone;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("pinecone")
@DisplayName("Pinecone")
@ExternalLib(name = "LangChain4J Pinecone",
    type=DEPENDENCY,
    description = "LangChain4J Pinecone",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-pinecone:1.1.0-beta7")
public class PineconeStoreConnectionProvider implements BaseStoreConnectionProvider {

  private PineconeStoreConnection connection;

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private PineconeStoreConnectionParameters pineconeStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {
    return connection;
  }


  @Override
  public void dispose() {

  }

  @Override
  public void initialise() throws InitialisationException {
    connection = new PineconeStoreConnection(pineconeStoreConnectionParameters);
    connection.initialise();
  }
}
