package org.mule.extension.vectors.internal.connection.embeddings.mistralai;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class MistralAIModelConnectionParameters {

    @Parameter
    @Password
    @Expression(ExpressionSupport.SUPPORTED)
    @Placement(order = 1)
    @Example("<your-api-key>")
    private String apiKey;

    @Parameter
    @Optional(defaultValue = "60000")
    @DisplayName("Total Timeout (ms)")
    @Summary("Total timeout in milliseconds for API requests. Set to 60000ms (60 seconds) by default.")
    private long totalTimeout;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public long getTotalTimeout() {
        return totalTimeout;
    }

    public void setTotalTimeout(long totalTimeout) {
        this.totalTimeout = totalTimeout;
    }
}
