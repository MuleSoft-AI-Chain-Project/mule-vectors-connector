package org.mule.extension.vectors.internal.connection.embeddings.huggingface;


import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;

import org.mule.runtime.extension.api.annotation.Alias;

import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;


@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnectionProvider extends BaseModelConnectionProvider  {


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
