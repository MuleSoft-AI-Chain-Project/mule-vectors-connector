package org.mule.extension.vectors.internal.connection.model;

import java.util.List;

public interface BaseImageModelConnection extends BaseModelConnection{
    
    Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName);

    default Object generateMultimodalEmbeddings(List<String> text, List<byte[]> imageBytesList, String modelName) {
      
      throw new UnsupportedOperationException("Unimplemented method 'generateEmbeddings(String text, byte[] imageBytes, String modelName)'");
    }
}
