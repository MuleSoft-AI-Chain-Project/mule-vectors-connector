package org.mule.extension.vectors.internal.connection.model;

import java.util.List;

import org.mule.extension.vectors.internal.data.embedding.Embedding;
import org.mule.extension.vectors.internal.data.segment.TextSegment;
import org.mule.extension.vectors.internal.model.output.Response;
import org.mule.extension.vectors.internal.util.ValidationUtils;

import static java.util.Collections.singletonList;

public interface BaseTextModelConnection extends BaseModelConnection{
    
    /**
     * Embed a text.
     *
     * @param text the text to embed.
     * @return the embedding.
     */
    default Response<Embedding> embed(String text) {
        return embed(TextSegment.from(text));
    }

    /**
     * Embed the text content of a TextSegment.
     *
     * @param textSegment the text segment to embed.
     * @return the embedding.
     */
    default Response<Embedding> embed(TextSegment textSegment) {
        Response<List<Embedding>> response = embedAll(singletonList(textSegment));
        ValidationUtils.ensureEq(response.content().size(), 1,
                "Expected a single embedding, but got %d", response.content().size());
        return Response.from(response.content().get(0), response.tokenUsage(), response.finishReason());
    }

    /**
     * Embeds the text content of a list of TextSegments.
     *
     * @param textSegments the text segments to embed.
     * @return the embeddings.
     */
    Response<List<Embedding>> embedAll(List<TextSegment> textSegments);

    /**
     * Returns the dimension of the {@link Embedding} produced by this embedding model.
     *
     * @return dimension of the embedding
     */
    default int dimension() {
        return embed("test").content().dimension();
    }
}
