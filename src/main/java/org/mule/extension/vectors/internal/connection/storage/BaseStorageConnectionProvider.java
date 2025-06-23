package org.mule.extension.vectors.internal.connection.storage;

import com.mulesoft.connectors.commons.template.connection.provider.ConnectorConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;

public abstract class BaseStorageConnectionProvider implements
    ConnectorConnectionProvider<BaseStorageConnection>, Initialisable, Disposable {

}
