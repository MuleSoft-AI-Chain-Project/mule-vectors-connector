package org.mule.extension.vectors.internal.storage.gcs;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.storage.BaseStorageConfiguration;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("googleCloudStorage")
@DisplayName("Google Cloud Storage")
public class GoogleCloudStorageConfiguration implements BaseStorageConfiguration {

    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 1)
    private String gcsKeyFilePath;

    @Override
    public String getStorageType() {
        return Constants.STORAGE_TYPE_GCS;
    }

    public String getGcsKeyFilePath() {
        return gcsKeyFilePath;
    }

    public void setGcsKeyFilePath(String gcsKeyFilePath) {
        this.gcsKeyFilePath = gcsKeyFilePath;
    }
}
