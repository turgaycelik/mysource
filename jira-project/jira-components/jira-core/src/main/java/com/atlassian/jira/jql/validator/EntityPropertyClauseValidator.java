package com.atlassian.jira.jql.validator;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.constants.PropertyClauseInformation;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

/**
 * Base class for ClauseValidators applied to property clauses.
 * @since 6.2
 */
public abstract class EntityPropertyClauseValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final String propertyType;

    public EntityPropertyClauseValidator(final String propertyType)
    {
        this.propertyType = propertyType;
        this.supportedOperatorsValidator = new SupportedOperatorsValidator(supportedOperators());
    }

    @Nonnull
    @Override
    public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
    {
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            if (terminalClause.getProperty().isEmpty())
            {
                errors.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.property.query", propertyType, terminalClause.getName()));
            }
            else if (!terminalClause.getName().equals(propertyType))
            {
                errors.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.unknown.property", terminalClause.getName(), propertyType));
            }
        }
        return errors;
    }

    protected I18nHelper getI18n(User user)
    {
        return ComponentAccessor.getI18nHelperFactory().getInstance(user);
    }

    public static boolean isSupportedOperator(final Operator operator)
    {
        return supportedOperators().contains(operator);
    }

    public static Collection<Operator> supportedOperators()
    {
        return PropertyClauseInformation.operators;
    }

}
