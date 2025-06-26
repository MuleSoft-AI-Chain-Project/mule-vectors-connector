package org.mule.extension.vectors.internal.helper.document;

import java.io.InputStream;

public interface DocumentParser {

  String parse(InputStream inputStream);
}
