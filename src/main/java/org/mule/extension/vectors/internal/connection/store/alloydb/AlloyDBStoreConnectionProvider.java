package org.mule.extension.vectors.internal.connection.store.alloydb;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("alloyDB")
@DisplayName("AlloyDB")
public class AlloyDBStoreConnectionProvider  extends BaseStoreConnectionProvider {
  
  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private AlloyDBStoreConnectionParameters alloyDBStoreConnectionParameters;
  
  @Override
  public BaseStoreConnection connect() throws ConnectionException {
    
    try {
      
      AlloyDBStoreConnection alloyDBStoreConnection =
          new AlloyDBStoreConnection(alloyDBStoreConnectionParameters.getProjectId(),
                                    alloyDBStoreConnectionParameters.getRegion(),
                                    alloyDBStoreConnectionParameters.getCluster(),
                                    alloyDBStoreConnectionParameters.getInstance(),
                                    alloyDBStoreConnectionParameters.getIamAccountEmail(),
                                    alloyDBStoreConnectionParameters.getHost(),
                                    alloyDBStoreConnectionParameters.getIpType(),
                                    alloyDBStoreConnectionParameters.getPort(),
                                    alloyDBStoreConnectionParameters.getDatabase(),
                                    alloyDBStoreConnectionParameters.getUser(),
                                    alloyDBStoreConnectionParameters.getPassword());
      alloyDBStoreConnection.connect();
      return alloyDBStoreConnection;
      
    } catch (Exception e) {
      
      throw new ConnectionException("Failed to connect to AlloyDB.", e);
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
        return ConnectionValidationResult.failure("Failed to validate connection to PGVector", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to PGVector", e);
    }
  }
  
}
