package org.mule.extension.vectors.internal.connection.embeddings.nomic;


import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
