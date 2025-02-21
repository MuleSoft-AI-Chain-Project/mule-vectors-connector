package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

import java.util.List;

public class AzureAIVisionModelResponse {

  private List<Model> value;

  public List<Model> getValue() {
    return value;
  }

  public void setValue(List<Model> value) {
    this.value = value;
  }

  public static class Model {
    private String name;
    private String createdDateTime;
    private String updatedDateTime;
    private String status;
    private TrainingParameters trainingParameters;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getCreatedDateTime() {
      return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
      this.createdDateTime = createdDateTime;
    }

    public String getUpdatedDateTime() {
      return updatedDateTime;
    }

    public void setUpdatedDateTime(String updatedDateTime) {
      this.updatedDateTime = updatedDateTime;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public TrainingParameters getTrainingParameters() {
      return trainingParameters;
    }

    public void setTrainingParameters(TrainingParameters trainingParameters) {
      this.trainingParameters = trainingParameters;
    }
  }

  public static class TrainingParameters {
    private int timeBudgetInHours;
    private String trainingDatasetName;

    public int getTimeBudgetInHours() {
      return timeBudgetInHours;
    }

    public void setTimeBudgetInHours(int timeBudgetInHours) {
      this.timeBudgetInHours = timeBudgetInHours;
    }

    public String getTrainingDatasetName() {
      return trainingDatasetName;
    }

    public void setTrainingDatasetName(String trainingDatasetName) {
      this.trainingDatasetName = trainingDatasetName;
    }
  }
}
