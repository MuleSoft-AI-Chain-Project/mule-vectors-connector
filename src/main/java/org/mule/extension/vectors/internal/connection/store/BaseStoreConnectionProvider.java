package org.mule.extension.vectors.internal.connection.store;

import com.mulesoft.connectors.commons.template.connection.provider.ConnectorConnectionProvider;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;

public interface  BaseStoreConnectionProvider
    extends  ConnectorConnectionProvider<BaseStoreConnection>, Initialisable, Disposable {

}
