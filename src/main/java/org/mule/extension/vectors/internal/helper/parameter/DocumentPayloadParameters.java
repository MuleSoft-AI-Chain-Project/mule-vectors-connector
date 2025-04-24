package org.mule.extension.vectors.internal.helper.parameter;

import java.io.InputStream;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.provider.FileTypeProvider;
import org.mule.extension.vectors.internal.helper.provider.PayloadContentTypeProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;

public class DocumentPayloadParameters {

  @Parameter
  @Alias("content")
  @DisplayName("Content")
  @Summary("The payload content .")
  @Placement(order = 1)
  @Expression(ExpressionSupport.SUPPORTED)
  private @Content(primary = true) InputStream content;

  @Parameter
  @Alias("format")
  @DisplayName("Format")
  @Summary("The payload content format.")
  @Placement(order = 2)
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(PayloadContentTypeProvider.class)
  @Optional(defaultValue = Constants.PAYLOAD_CONTENT_TYPE_BINARY)
  private String format;

  @Parameter
  @Alias("fileType")
  @DisplayName("File Type")
  @Summary("The supported types of file.")
  @Placement(order = 3)
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(FileTypeProvider.class)
  @Optional(defaultValue = Constants.FILE_TYPE_TEXT)
  private String fileType;

  @Parameter
  @Alias("fileName")
  @DisplayName("File name")
  @Summary("The file name.")
  @Placement(order = 4)
  @Expression(ExpressionSupport.SUPPORTED)
  @Example("example.pdf")
  @Optional
  private String fileName;

  public InputStream getContent() {
    return content;
  }

  public String getFormat() {
    return format;
  }

  public String getFileType() {
    return fileType;
  }

  public String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return "DocumentPayloadParameters{" +
        "format='" + format + '\'' +
        ", fileType='" + fileType + '\'' +
        ", fileName='" + fileName + '\'' +
        '}';
  }

}
