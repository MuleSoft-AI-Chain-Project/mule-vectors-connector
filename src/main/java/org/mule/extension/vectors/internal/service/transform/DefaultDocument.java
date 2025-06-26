package org.mule.extension.vectors.internal.service.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A default implementation of a {@link Document}.
 */
public class DefaultDocument implements Document {

    private final String text;
    private final Map<String, Object> metadata;

    public DefaultDocument(String text, Map<String, Object> metadata) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Document text must not be null or blank");
        }
        this.text = text;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public DefaultDocument(String text) {
        this(text, new HashMap<>());
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public Map<String, Object> metadata() {
        return new HashMap<>(metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DefaultDocument) obj;
        return Objects.equals(this.text, that.text) &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, metadata);
    }

    @Override
    public String toString() {
        return "DefaultDocument {" +
                " text = '" + text + '\'' +
                ", metadata = " + metadata +
                " }";
    }
} 