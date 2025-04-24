package org.mule.extension.vectors.internal.connection.store.ephemeralfile;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import com.typesafe.config.Optional;

import static org.mule.runtime.api.meta.model.display.PathModel.Location.EXTERNAL;

public class EphemeralFileStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Optional
  @DisplayName("Working Directory")
  @Summary("Directory to be considered to create store files.")
  @org.mule.runtime.extension.api.annotation.param.display.Path(location = EXTERNAL)
  private String workingDir;

  public String getWorkingDir() {
    return workingDir;
  }
}
