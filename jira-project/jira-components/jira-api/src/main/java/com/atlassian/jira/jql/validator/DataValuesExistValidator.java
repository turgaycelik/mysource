package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * A clause validator that can be used for multiple constant (priority, status, resolution) clause types that
 * uses the {@link com.atlassian.jira.jql.resolver.NameResolver} to determine if the value exists.
 *
 */
class DataValuesExistValidator extends ValuesExistValidator
{
    private final NameResolver nameResolver;

    DataValuesExistValidator(final JqlOperandResolver operandResolver, NameResolver nameResolver, I18nHelper.BeanFactory beanFactory)
    {
        super(operandResolver, beanFactory);
        this.nameResolver = Assertions.notNull("nameResolver", nameResolver);
    }

    DataValuesExistValidator(final JqlOperandResolver operandResolver, NameResolver nameResolver, I18nHelper.BeanFactory beanFactory, MessageSet.Level level)
    {
        super(operandResolver, beanFactory, level);
        this.nameResolver = Assertions.notNull("nameResolver", nameResolver);
    }

    boolean stringValueExists(final User searcher, final String value)
    {
        return nameResolver.nameExists(value);
    }

    boolean longValueExist(final User searcher, final Long value)
    {
        return nameResolver.idExists(value);
    }
}
