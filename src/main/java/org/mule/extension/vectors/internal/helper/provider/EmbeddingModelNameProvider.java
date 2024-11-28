package org.mule.extension.vectors.internal.helper.provider;

import java.util.Collections;
import java.util.Set;

import org.mule.extension.vectors.internal.config.Configuration;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

public class EmbeddingModelNameProvider implements ValueProvider {

  @Connection
  @Alias("modelConnection")
  private BaseModelConnection modelConnection;

  private static final Set<Value> VALUES_FOR_AZURE_OPENAI = ValueBuilder.getValuesFor(
      Constants.EMBEDDING_MODEL_NAME_TEXT_EMBEDDING_3_SMALL,
      Constants.EMBEDDING_MODEL_NAME_TEXT_EMBEDDING_3_LARGE,
      Constants.EMBEDDING_MODEL_NAME_TEXT_EMBEDDING_ADA_002
  );

  private static final Set<Value> VALUES_FOR_OPENAI = ValueBuilder.getValuesFor(
      Constants.EMBEDDING_MODEL_NAME_TEXT_EMBEDDING_3_SMALL,
      Constants.EMBEDDING_MODEL_NAME_TEXT_EMBEDDING_3_LARGE,
      Constants.EMBEDDING_MODEL_NAME_TEXT_EMBEDDING_ADA_002
  );

  private static final Set<Value> VALUES_FOR_MISTRAL_AI = ValueBuilder.getValuesFor(
      Constants.EMBEDDING_MODEL_NAME_MISTRAL_EMBED
  );

  private static final Set<Value> VALUES_FOR_NOMIC = ValueBuilder.getValuesFor(
      Constants.EMBEDDING_MODEL_NAME_NOMIC_EMBED_TEXT
  );

  private static final Set<Value> VALUES_FOR_HUGGING_FACE = ValueBuilder.getValuesFor(
      Constants.EMBEDDING_MODEL_NAME_FALCON_7B_INSTRUCT,
      Constants.EMBEDDING_MODEL_NAME_MINI_LM_L6_V2
  );

  private static final Set<Value> VALUES_FOR_EINSTEIN = ValueBuilder.getValuesFor(
      Constants.EMBEDDING_MODEL_NAME_SFDC_TEXT_EMBEDDING_ADA_002,
      Constants.EMBEDDING_MODEL_NAME_SFDC_AZURE_TEXT_EMBEDDING_ADA_002,
      Constants.EMBEDDING_MODEL_NAME_SFDC_OPENAI_TEXT_EMBEDDING_ADA_002
  );

  @Override
  public Set<Value> resolve() throws ValueResolvingException {

    String embeddingModelService = modelConnection.getEmbeddingModelService();
    switch (embeddingModelService) {
      case Constants.EMBEDDING_MODEL_SERVICE_OPENAI:
        return VALUES_FOR_OPENAI;
      case Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI:
        return VALUES_FOR_AZURE_OPENAI;
      case Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI:
        return VALUES_FOR_MISTRAL_AI;
      case Constants.EMBEDDING_MODEL_SERVICE_NOMIC:
        return VALUES_FOR_NOMIC;
      case Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE:
        return VALUES_FOR_HUGGING_FACE;
      case Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN:
        return VALUES_FOR_EINSTEIN;
      default:
        return Collections.emptySet();
    }
  }

}
