package org.mule.extension.vectors.internal.service.store.aisearch;

import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchFilterMapper;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsNotEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThan;
import dev.langchain4j.store.embedding.filter.comparison.IsGreaterThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThan;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThanOrEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import dev.langchain4j.store.embedding.filter.comparison.IsNotIn;
import dev.langchain4j.store.embedding.filter.comparison.ContainsString;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Not;
import dev.langchain4j.store.embedding.filter.logical.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Maps {@link Filter} objects to Azure AI Search filter strings.
 * Use the default structure of the Azure AI Search Index.
 */
public class VectorsAzureAiSearchFilterMapper implements AzureAiSearchFilterMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(VectorsAzureAiSearchFilterMapper.class);
    private static final String UNSUPPORTED_ERROR_MESSAGE = "Unsupported filter type: ";

    public VectorsAzureAiSearchFilterMapper() {
        //Empty constructor
    }

    public String map(Filter filter) {

        if (filter == null) return "";

        String azureAISearchFilter = isLogicalOperator(filter) ? mapLogicalOperator(filter) : mapComparisonFilter(filter);
        LOGGER.debug("LangChain4j Filter: {}", filter);
        LOGGER.debug("Azure AI Search Filter: {}", azureAISearchFilter);
        return azureAISearchFilter;
    }


    private String mapLogicalOperator(Filter operator) {
        if (operator instanceof And) return  format(getLogicalFormat(operator), map(((And) operator).left()), map(((And) operator).right()));
        if (operator instanceof Or) return format(getLogicalFormat(operator), map(((Or) operator).left()), map(((Or) operator).right()));
        if (operator instanceof Not) return format(getLogicalFormat(operator), map(((Not) operator).expression()));
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE + operator.getClass().getName());
    }

    private boolean isLogicalOperator(Filter filter) {
        return filter instanceof And || filter instanceof Or || filter instanceof Not;
    }

    private String mapComparisonFilter(Filter filter) {
        if (filter instanceof IsEqualTo) return mapIsEqualTo((IsEqualTo) filter);
        if (filter instanceof IsNotEqualTo) return mapIsNotEqualTo((IsNotEqualTo) filter);
        if (filter instanceof IsGreaterThan) return mapIsGreaterThan((IsGreaterThan) filter);
        if (filter instanceof IsGreaterThanOrEqualTo) return mapIsGreaterThanOrEqualTo((IsGreaterThanOrEqualTo) filter);
        if (filter instanceof IsLessThan) return mapIsLessThan((IsLessThan) filter);
        if (filter instanceof IsLessThanOrEqualTo) return mapIsLessThanOrEqualTo((IsLessThanOrEqualTo) filter);
        if (filter instanceof IsIn) return mapIsIn((IsIn) filter);
        if (filter instanceof IsNotIn) return mapIsNotIn((IsNotIn) filter);
        if (filter instanceof ContainsString) return mapContainsString((ContainsString) filter);
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE + filter.getClass().getName());
    }

    private String getLogicalFormat(Filter filter) {
        if (filter instanceof And) return "(%s and %s)";
        if (filter instanceof Or) return "(%s or %s)";
        if (filter instanceof Not) return "(not %s)";
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE + filter.getClass().getName());
    }

    private String getComparisonFormat(Filter filter) {
        if (filter instanceof IsEqualTo) return "k/value eq '%s'";
        if (filter instanceof IsGreaterThan) return "k/value gt '%s'";
        if (filter instanceof IsGreaterThanOrEqualTo) return "k/value ge '%s'";
        if (filter instanceof IsLessThan) return "k/value lt '%s'";
        if (filter instanceof IsLessThanOrEqualTo) return "k/value le '%s'";
        if (filter instanceof IsIn) return "search.in(k/value, ('%s'))";
        if (filter instanceof ContainsString) return "search.ismatch('%s', 'metadata/attributes/value', 'simple', 'all')";
        // not use, it raplace by Not ( IsIn )
        //        if (filter instanceof IsNotIn) return "not search.in(k/value, ('%s'))";
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE + filter.getClass().getName());
    }

    private String mapIsNotIn(IsNotIn filter) {
        return map(Filter.not(new IsIn(filter.key(), filter.comparisonValues())));
    }

    private String mapIsIn(IsIn filter) {
        return formatComparisonFilter(filter.key(), mapSearchInValues(filter.comparisonValues()), getComparisonFormat(filter));
    }

    private String mapIsLessThanOrEqualTo(IsLessThanOrEqualTo filter) {
        return formatComparisonFilter(filter.key(), filter.comparisonValue().toString(), getComparisonFormat(filter));
    }

    private String mapIsLessThan(IsLessThan filter) {
        return formatComparisonFilter(filter.key(), filter.comparisonValue().toString(), getComparisonFormat(filter));
    }

    private String mapIsGreaterThanOrEqualTo(IsGreaterThanOrEqualTo filter) {
        return formatComparisonFilter(filter.key(), filter.comparisonValue().toString(), getComparisonFormat(filter));
    }

    private String mapIsGreaterThan(IsGreaterThan filter) {
        return formatComparisonFilter(filter.key(), filter.comparisonValue().toString(), getComparisonFormat(filter));
    }

    private String mapIsEqualTo(IsEqualTo filter) {
        return formatComparisonFilter(filter.key(), filter.comparisonValue().toString(), getComparisonFormat(filter));
    }

    private String mapIsNotEqualTo(IsNotEqualTo filter) {
        return map(Filter.not(new IsEqualTo(filter.key(), filter.comparisonValue())));
    }

    private String mapContainsString(ContainsString filter) {
        return formatContainsStringFilter(filter.key(), filter.comparisonValue().toString(), getComparisonFormat(filter));
    }

    private String mapSearchInValues(Collection<?> comparisonValues) {
        return comparisonValues.stream().map(Object::toString).sorted().collect(Collectors.joining(", "));
    }

    private String formatComparisonFilter(String key, String value, String format) {
        // format may already contain a %s for value, so we need to fill key and value in order
        String pattern = "metadata/attributes/any(k: k/key eq '%s' and %s)";
        return String.format(pattern, key, String.format(format, value));
    }

    private String formatContainsStringFilter(String key, String value, String format) {
        String pattern = "(metadata/attributes/any(k: k/key eq '%s') and (%s))";
        return String.format(pattern, key, String.format(format, value));
    }
}
