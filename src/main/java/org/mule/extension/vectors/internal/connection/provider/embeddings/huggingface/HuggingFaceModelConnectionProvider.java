package org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface;

import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnectionProvider extends BaseModelConnectionProvider {


  @ParameterGroup(name = CONNECTION)
  private HuggingFaceModelConnectionParameters huggingFaceModelConnectionParameters;


  @Override
  public BaseModelConnection connect() throws ConnectionException {

    HuggingFaceModelConnection huggingFaceModelConnection = new HuggingFaceModelConnection(
                                                                                           huggingFaceModelConnectionParameters
                                                                                               .getApiKey(),
                                                                                           huggingFaceModelConnectionParameters
                                                                                               .getTimeout(),
                                                                                           getHttpClient());
    return huggingFaceModelConnection;


  }

}
