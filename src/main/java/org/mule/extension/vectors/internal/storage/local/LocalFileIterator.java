package org.mule.extension.vectors.internal.storage.local;

import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class LocalFileIterator implements FileIterator {
    private final LocalStorage localClient;
    private final Iterator<Path> pathIterator;

    public LocalFileIterator(LocalStorage localClient, List<Path> files) {
        this.localClient = localClient;
        this.pathIterator = files.iterator();
    }

    @Override
    public boolean hasNext() {
        return pathIterator.hasNext();
    }

    @Override
    public File next() {
        if (hasNext()) {
            Path path = pathIterator.next();
            InputStream content = localClient.loadFile(path);
            return new File(content, path.toString(), LocalStorage.parseFileName(path.toString()));
        }
        return null;
    }
} 