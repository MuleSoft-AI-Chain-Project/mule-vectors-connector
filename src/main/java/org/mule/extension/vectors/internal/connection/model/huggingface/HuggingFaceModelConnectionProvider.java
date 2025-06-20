package org.mule.extension.vectors.internal.connection.model.huggingface;


import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;

import org.mule.runtime.extension.api.annotation.Alias;

import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnectionProvider extends BaseModelConnectionProvider  {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuggingFaceModelConnectionProvider.class);


  @ParameterGroup(name = CONNECTION)
  private HuggingFaceModelConnectionParameters huggingFaceModelConnectionParameters;


  @Override
  public BaseModelConnection connect() throws ConnectionException {

      HuggingFaceModelConnection huggingFaceModelConnection = new HuggingFaceModelConnection(
          huggingFaceModelConnectionParameters.getApiKey(),
          huggingFaceModelConnectionParameters.getTotalTimeout(),
          getHttpClient());
      return huggingFaceModelConnection;


  }

}
