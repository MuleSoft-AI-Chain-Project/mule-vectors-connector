package org.mule.extension.vectors.internal.store.milvus;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.store.BaseStoreConnection;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class MilvusStoreConnection implements BaseStoreConnection {

  private String url;

  public MilvusStoreConnection(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String getVectorStore() {

    return Constants.VECTOR_STORE_MILVUS;
  }
}
