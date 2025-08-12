package org.mule.extension.vectors.internal.connection.storage;


import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;

public abstract class BaseStorageConnectionProvider implements
    CachedConnectionProvider<BaseStorageConnection>, Initialisable, Disposable {

}
