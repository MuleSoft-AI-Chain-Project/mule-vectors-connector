package org.mule.extension.vectors.internal.transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a segment of text with associated metadata, similar to langchain4j's TextSegment.
 */
public class TextSegment {
    private final String text;
    private final Map<String, Object> metadata;

    /**
     * Constructs a TextSegment with text and metadata.
     * @param text The text content (must not be null or blank)
     * @param metadata The metadata map (may be null)
     */
    public TextSegment(String text, Map<String, Object> metadata) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("TextSegment text must not be null or blank");
        }
        this.text = text;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    /**
     * Constructs a TextSegment with text and empty metadata.
     * @param text The text content (must not be null or blank)
     */
    public TextSegment(String text) {
        this(text, null);
    }

    public String text() {
        return text;
    }

    public Map<String, Object> metadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public Object metadata(String key) {
        return metadata.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextSegment that = (TextSegment) o;
        return Objects.equals(text, that.text) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, metadata);
    }

    @Override
    public String toString() {
        return "TextSegment{" +
                "text='" + text + '\'' +
                ", metadata=" + metadata +
                '}';
    }

    /**
     * Static factory method for creating a TextSegment from text only.
     */
    public static TextSegment from(String text) {
        return new TextSegment(text);
    }

    /**
     * Static factory method for creating a TextSegment from text and metadata.
     */
    public static TextSegment from(String text, Map<String, Object> metadata) {
        return new TextSegment(text, metadata);
    }
} 