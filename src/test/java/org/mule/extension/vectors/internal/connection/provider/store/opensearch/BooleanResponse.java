package org.mule.extension.vectors.internal.connection.provider.store.opensearch;

public class BooleanResponse {

  private final boolean value;

  private BooleanResponse(boolean value) {
    this.value = value;
  }

  public static BooleanResponse of(boolean value) {
    return new BooleanResponse(value);
  }

  public boolean value() {
    return value;
  }
}
