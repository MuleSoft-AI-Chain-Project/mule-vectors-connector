package org.mule.extension.vectors.internal.helper.media;

import java.io.IOException;

public interface MediaProcessor {

  byte[] process(byte[] media, String format) throws IOException;

  byte[] process(byte[] media) throws IOException;
}
