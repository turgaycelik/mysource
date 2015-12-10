package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.List;

/**
 * A clause validator that can be used for multiple constant (priority, status, resolution) clause types that uses the
 * {@link com.atlassian.jira.jql.resolver.IndexInfoResolver} to determine if the value exists.
 *
 */
class RawValuesExistValidator extends ValuesExistValidator
{
    private final IndexInfoResolver indexInfoResolver;

    RawValuesExistValidator(final JqlOperandResolver operandResolver, IndexInfoResolver indexInfoResolver, I18nHelper.BeanFactory beanFactory)
    {
        super(operandResolver, beanFactory);
        this.indexInfoResolver = Assertions.notNull("indexInfoResolver", indexInfoResolver);
    }

    boolean stringValueExists(final User searcher, final String value)
    {
        List indexValues = indexInfoResolver.getIndexedValues(value);
        return indexValues != null && !indexValues.isEmpty();
    }

    boolean longValueExist(final User searcher, final Long value)
    {
        List indexValues = indexInfoResolver.getIndexedValues(value);
        return indexValues != null && !indexValues.isEmpty();
    }
}
