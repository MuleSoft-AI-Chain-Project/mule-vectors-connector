package org.mule.extension.vectors.internal.connection.model.vertexai;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.threeten.bp.Duration;

public class VertexAIModelConnectionParameters extends BaseModelConnectionParameters {

    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 1)
    @Example("<your-project-id>")
    private String projectId;

    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 2)
    @Example("us-central1")
    private String location;

    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 3)
    @Example("<your-client-email>")
    private String clientEmail;

    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 4)
    @Example("<your-client-id>")
    private String clientId;

    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 5)
    @Example("<your-private-key-id>")
    private String privateKeyId;

    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 6)
    @Example("<your-private-key>")
    private String privateKey;

    @Parameter
    @DisplayName("Max attemps")
    @Summary("Maximum number of retries")
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 1, tab = Placement.ADVANCED_TAB)
    @Example("3")
    @Optional(defaultValue = "3")
    private int maxAttempts;

    @Parameter
    @DisplayName("Initial retry delay")
    @Summary("Initial retry delay in milliseconds")
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 2, tab = Placement.ADVANCED_TAB)
    @Example("500")
    @Optional(defaultValue = "500")
    private long initialRetryDelay;

    @Parameter
    @DisplayName("Retry delay multiplier")
    @Summary("Multiplier for subsequent retries")
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 3, tab = Placement.ADVANCED_TAB)
    @Example("1.5")
    @Optional(defaultValue = "1.5")
    private double retryDelayMultiplier;

    @Parameter
    @DisplayName("Maximum retry delay")
    @Summary("Maximum retry delay in milliseconds")
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 4, tab = Placement.ADVANCED_TAB)
    @Example("5000")
    @Optional(defaultValue = "5000")
    private long maxRetryDelay;

    @Parameter
    @DisplayName("Total timeout")
    @Summary("Total timeout for the operation in milliseconds")
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 5, tab = Placement.ADVANCED_TAB)
    @Example("60000")
    @Optional(defaultValue = "60000")
    private long totalTimeout;

    public String getProjectId() {
        return projectId;
    }

    public String getLocation() { return location; }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPrivateKeyId() {
        return privateKeyId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getInitialRetryDelay() {
        return initialRetryDelay;
    }

    public double getRetryDelayMultiplier() {
        return retryDelayMultiplier;
    }

    public long getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public long getTotalTimeout() {
        return totalTimeout;
    }
}
