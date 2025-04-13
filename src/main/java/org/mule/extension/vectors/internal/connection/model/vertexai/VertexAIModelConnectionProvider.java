package org.mule.extension.vectors.internal.connection.model.vertexai;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("googleVertexAI")
@DisplayName("Google Vertex AI")
public class VertexAIModelConnectionProvider extends BaseModelConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIModelConnectionProvider.class);

    @ParameterGroup(name = Placement.CONNECTION_TAB)
    private VertexAIModelConnectionParameters vertexAIModelConnectionParameters;

    @Override
    public BaseModelConnection connect() throws ConnectionException {
        try {
            LOGGER.info("Connecting to Google Vertex AI with parameters: {}", vertexAIModelConnectionParameters);
            VertexAIModelConnection vertexAIModelConnection = new VertexAIModelConnection(
                vertexAIModelConnectionParameters.getProjectId(),
                vertexAIModelConnectionParameters.getLocation(),
                vertexAIModelConnectionParameters.getClientEmail(),
                vertexAIModelConnectionParameters.getClientId(),
                vertexAIModelConnectionParameters.getPrivateKeyId(),
                vertexAIModelConnectionParameters.getPrivateKey(),
                vertexAIModelConnectionParameters.getMaxAttempts(),
                vertexAIModelConnectionParameters.getInitialRetryDelay(),
                vertexAIModelConnectionParameters.getRetryDelayMultiplier(),
                vertexAIModelConnectionParameters.getMaxRetryDelay(),
                vertexAIModelConnectionParameters.getTotalTimeout()
            );
            vertexAIModelConnection.connect();
            return vertexAIModelConnection;
        } catch (ConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionException("Failed to connect to Google Cloud Storage.", e);
        }
    }

    @Override
    public void disconnect(BaseModelConnection connection) {
        connection.disconnect();
    }

    @Override
    public ConnectionValidationResult validate(BaseModelConnection connection) {
        try {
            if (connection.isValid()) {
                return ConnectionValidationResult.success();
            } else {
                return ConnectionValidationResult.failure("Failed to validate connection to Google Cloud Storage", null);
            }
        } catch (Exception e) {
            return ConnectionValidationResult.failure("Failed to validate connection to Google Cloud Storage", e);
        }
    }
}
