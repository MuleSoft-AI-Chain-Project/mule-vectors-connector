package org.mule.extension.vectors.internal.connection.model;

import java.util.List;

public interface BaseTextModelConnection extends BaseModelConnection{
    
    Object generateEmbeddings(List<String> inputs, String modelName);
}
