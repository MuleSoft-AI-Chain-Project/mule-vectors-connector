package org.mule.extension.vectors.api.metadata;

import java.io.Serializable;

public class TokenUsage implements Serializable {

  private final int inputCount;
  private final int outputCount;
  private final int totalCount;

  public TokenUsage(int inputCount, int outputCount, int totalCount) {
    this.inputCount = inputCount;
    this.outputCount = outputCount;
    this.totalCount = totalCount;
  }

  public int getInputCount() {
    return inputCount;
  }

  public int getOutputCount() {
    return outputCount;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    TokenUsage that = (TokenUsage) o;

    if (inputCount != that.inputCount)
      return false;
    if (outputCount != that.outputCount)
      return false;
    return totalCount == that.totalCount;
  }

  public int hashCode() {
    int result = inputCount;
    result = 31 * result + outputCount;
    result = 31 * result + totalCount;
    return result;
  }
}
