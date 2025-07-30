package org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile;

import static org.mule.runtime.api.meta.model.display.PathModel.Location.EXTERNAL;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.DEFAULT_TAB;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import com.typesafe.config.Optional;

public class EphemeralFileStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Placement(tab = DEFAULT_TAB, order = 1)
  @Optional
  @DisplayName("Working Directory")
  @Summary("This is vector store is supported as beta. Please refer to the product documentation.")
  @org.mule.runtime.extension.api.annotation.param.display.Path(location = EXTERNAL)
  private String workingDir;

  public String getWorkingDir() {
    return workingDir;
  }
}
