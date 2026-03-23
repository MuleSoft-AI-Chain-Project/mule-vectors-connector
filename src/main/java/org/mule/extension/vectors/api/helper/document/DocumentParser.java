package org.mule.extension.vectors.api.helper.document;

import java.io.InputStream;

public interface DocumentParser {

  InputStream parse(InputStream inputStream);
}
