package org.mule.extension.vectors.internal.connection.storage.local;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("local")
@DisplayName("Local")
@ExternalLib(name = "LangChain4J Document Transformer Jsoup",
    type=DEPENDENCY,
    description = "LangChain4J Document Transformer Jsoup",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer",
    coordinates = "dev.langchain4j:langchain4j-document-transformer-jsoup:1.0.1-beta6")
public class LocalStorageConnectionProvider extends BaseStorageConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private LocalStorageConnectionParameters localStorageConnectionParameters;

  @Override
  public BaseStorageConnection connect() throws ConnectionException {
    try {

      LocalStorageConnection localStorageConnection = new LocalStorageConnection(localStorageConnectionParameters.getWorkingDir());
      localStorageConnection.connect();
      return localStorageConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to access Local File System.", e);
    }
  }

  @Override
  public void disconnect(BaseStorageConnection connection) {

    connection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(BaseStorageConnection connection) {

    try {

      if (connection.isValid()) {
        return ConnectionValidationResult.success();
      } else {
        return ConnectionValidationResult.failure("Failed to validate access to Local File System", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate access to Local File System", e);
    }
  }
}
