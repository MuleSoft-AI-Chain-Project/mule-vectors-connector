package org.mule.extension.vectors.internal.connection.model;

public interface BaseImageModelConnection extends BaseModelConnection{
    
    Object generateEmbeddings(byte[] imageBytes, String modelName);

    default Object generateEmbeddings(String text, byte[] imageBytes, String modelName) {
      
      throw new UnsupportedOperationException("Unimplemented method 'generateEmbeddings(String text, byte[] imageBytes, String modelName)'");
    }
}
