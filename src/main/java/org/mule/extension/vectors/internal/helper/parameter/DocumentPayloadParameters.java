package org.mule.extension.vectors.internal.helper.parameter;

import java.io.InputStream;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.provider.FileParserTypeProvider;
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
  @Alias("fileParserType")
  @DisplayName("File Parser type")
  @Summary("The file parser types.")
  @Placement(order = 3)
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(FileParserTypeProvider.class)
  @Optional(defaultValue = Constants.FILE_PARSER_TYPE_APACHE_TIKA)
  private String fileParserType;

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

    return fileParserType.equals(Constants.FILE_PARSER_TYPE_TEXT) ?
        Constants.FILE_TYPE_TEXT :
        Constants.FILE_TYPE_ANY;
  }

  public String getFileParserType() { return fileParserType; }

  public String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return "DocumentPayloadParameters{" +
        "format='" + format + '\'' +
        ", fileParserType='" + fileParserType + '\'' +
        ", fileName='" + fileName + '\'' +
        '}';
  }

}
