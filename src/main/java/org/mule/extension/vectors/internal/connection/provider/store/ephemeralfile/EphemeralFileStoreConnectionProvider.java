package org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile;

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

@Alias("ephemeralFile")
@DisplayName("Ephemeral File")
@ExternalLib(name = "LangChain4J",
    type=DEPENDENCY,
    description = "LangChain4J",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j:1.0.1")
public class EphemeralFileStoreConnectionProvider implements BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private EphemeralFileStoreConnectionParameters ephemeralFileStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      EphemeralFileStoreConnection ephemeralFileStoreConnection =
          new EphemeralFileStoreConnection(ephemeralFileStoreConnectionParameters);
      return ephemeralFileStoreConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Chroma", e);
    }
  }

  @Override
  public void dispose() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public void initialise() throws InitialisationException {
    // HttpClient lifecycle is managed by the provider
  }
}
