package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.provider.FileTypeProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;

public class DocumentParameters {

  @Parameter
  @Alias("fileType")
  @DisplayName("File Type")
  @Summary("The supported types of file.")
  @Placement(order = 1)
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(FileTypeProvider.class)
  @Optional(defaultValue = Constants.FILE_TYPE_TEXT)
  private String fileType;

  @Parameter
  @Alias("contextPath")
  @DisplayName("Context Path")
  @Summary("The context path.")
  @Placement(order = 2)
  @Expression(ExpressionSupport.SUPPORTED)
  private String contextPath;

//  @Parameter
//  private String folderPath;

//  @Parameter
//  private String awsS3Bucket;

//  @Parameter
//  private String azureBlobContainer;

  public String getFileType() {
    return fileType;
  }

  public String getContextPath() {
    return contextPath;
  }
}
