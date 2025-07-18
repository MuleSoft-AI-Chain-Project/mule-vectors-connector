package org.mule.extension.vectors.internal.connection.provider.embeddings.nomic;


import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

@Alias("nomic")
@DisplayName("Nomic")
public class NomicModelConnectionProvider extends BaseModelConnectionProvider {


    @ParameterGroup(name = CONNECTION)
    private NomicModelConnectionParameters nomicModelConnectionParameters;



    @Override
    public BaseModelConnection connect() throws ConnectionException {

            NomicModelConnection nomicModelConnection = new NomicModelConnection(
                nomicModelConnectionParameters.getApiKey(),
                nomicModelConnectionParameters.getTotalTimeout(),
                getHttpClient()
            );
           return nomicModelConnection;
    }
}
