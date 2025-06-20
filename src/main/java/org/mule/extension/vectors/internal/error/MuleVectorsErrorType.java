package org.mule.extension.vectors.internal.error;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;


/**
 * Defines the custom error types for the Vectors Connector.
 * Each error is mapped to a parent {@link ErrorTypeDefinition} from the Mule SDK to enable more flexible error handling strategies in Mule flows.
 *
 * (c) 2003-2024 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
public enum MuleVectorsErrorType implements ErrorTypeDefinition<MuleVectorsErrorType> {

    // region Configuration and Connection Errors
    /**
     * The provided connection details are invalid or insufficient.
     */
    INVALID_CONNECTION(MuleErrors.CONNECTIVITY),

    /**
     * The provided configuration parameters are invalid.
     */
    INVALID_CONFIGURATION(MuleErrors.CONNECTIVITY),

    /**
     * The connector failed to establish a connection with the external service.
     */
    CONNECTION_FAILED(MuleErrors.CONNECTIVITY),

    // endregion

    // region Input Validation Errors
    /**
     * A required parameter is missing or invalid.
     */
    INVALID_PARAMETER(MuleErrors.VALIDATION),

    /**
     * The document provided for parsing is malformed or in an unsupported format.
     */
    DOCUMENT_PARSING_FAILURE(MuleErrors.VALIDATION),

    /**
     * The provided media data is malformed or in an unsupported format.
     */
    UNSUPPORTED_MEDIA_TYPE(MuleErrors.VALIDATION),
    // endregion

    // region Operational Errors
    /**
     * A general failure occurred during a document-related operation.
     */
    DOCUMENT_OPERATIONS_FAILURE(MuleErrors.ANY),

    /**
     * A general failure occurred during a media-related operation.
     */
    MEDIA_OPERATIONS_FAILURE(MuleErrors.ANY),

    /**
     * A general failure occurred during an embedding generation operation.
     */
    EMBEDDING_OPERATIONS_FAILURE(MuleErrors.ANY),

    /**
     * A general failure occurred during a vector store operation.
     */
    STORE_OPERATIONS_FAILURE(MuleErrors.ANY),

    /**
     * The requested operation is not supported by the configured vector store.
     */
    STORE_UNSUPPORTED_OPERATION(MuleErrors.ANY),
    // endregion

    // region External Service Errors
    /**
     * The external AI service returned an error.
     */
    AI_SERVICES_FAILURE(MuleErrors.CONNECTIVITY),

    /**
     * The request was rejected by the external AI service due to rate limiting.
     */
    AI_SERVICES_RATE_LIMITING_ERROR(MuleErrors.CONNECTIVITY),

    /**
     * The external storage service (e.g., S3, Azure Blob) returned an error.
     */
    STORAGE_SERVICES_FAILURE(MuleErrors.CONNECTIVITY),

    /**
     * The external vector store service returned an error.
     */
    STORE_SERVICES_FAILURE(MuleErrors.CONNECTIVITY);
    // endregion

    private final ErrorTypeDefinition<? extends Enum<?>> parent;

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
        return Optional.of(parent);
    }

    MuleVectorsErrorType(ErrorTypeDefinition<? extends Enum<?>> parent) {
        this.parent = parent;
    }
}
