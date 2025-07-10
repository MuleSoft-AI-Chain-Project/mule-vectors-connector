package org.mule.extension.vectors.internal.connection.model.nomic;


import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("nomic")
@DisplayName("Nomic")
public class NomicModelConnectionProvider extends BaseModelConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(NomicModelConnectionProvider.class);


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
