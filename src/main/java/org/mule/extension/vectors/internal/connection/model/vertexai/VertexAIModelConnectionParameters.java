package org.mule.extension.vectors.internal.connection.model.vertexai;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

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
    @DisplayName("Total timeout")
    @Summary("Total timeout for the operation in milliseconds")
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 1, tab = Placement.ADVANCED_TAB)
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

    public long getTotalTimeout() {
        return totalTimeout;
    }

    @Override
    public String toString() {
        return "VertexAIModelConnectionParameters{" +
                "projectId='" + projectId + '\'' +
                ", location='" + location + '\'' +
                ", clientEmail='" + clientEmail + '\'' +
                ", clientId='" + clientId + '\'' +
                ", privateKeyId='" + privateKeyId + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", totalTimeout=" + totalTimeout +
                '}';
    }
}
