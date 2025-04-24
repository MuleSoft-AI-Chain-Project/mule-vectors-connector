package org.mule.extension.vectors.internal.connection.model;

import java.util.List;

public interface BaseTextModelConnection extends BaseModelConnection{
    
    Object generateTextEmbeddings(List<String> inputs, String modelName);
}
