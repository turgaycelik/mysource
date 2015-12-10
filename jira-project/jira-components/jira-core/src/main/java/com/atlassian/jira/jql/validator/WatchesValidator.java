package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.WatchesIndexValueConverter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;

/**
 * Watches clause validator
 *
 * @since v4.4
 */
@InjectableComponent
public class WatchesValidator implements ClauseValidator
{
    private final IndexValuesValidator indexValuesValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlOperandResolver jqlOperandResolver;
    private final WatcherManager watcherManager;

    public WatchesValidator(final JqlOperandResolver jqlOperandResolver, final WatchesIndexValueConverter watchesIndexValueConverter, final WatcherManager watcherManager)
    {
        this.jqlOperandResolver = jqlOperandResolver;
        this.watcherManager = watcherManager;
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.indexValuesValidator = getIndexValuesValidator(watchesIndexValueConverter);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        if (watcherManager.isWatchingEnabled())
        {
            MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
            if (!errors.hasAnyErrors())
            {
                errors = indexValuesValidator.validate(searcher, terminalClause);
            }
            return errors;
        }
        else
        {
            MessageSet messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.watches.disabled", terminalClause.getName()));
            return messageSet;
        }
    }

    IndexValuesValidator getIndexValuesValidator(final WatchesIndexValueConverter watchesIndexValueConverter)
    {
        return new IndexValuesValidator(jqlOperandResolver, watchesIndexValueConverter, false)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, TerminalClause terminalClause, final QueryLiteral literal)
            {
                String fieldName = terminalClause.getName();
                final String literalString = literal.isEmpty() ? "EMPTY" : literal.asString();
                if (jqlOperandResolver.isFunctionOperand(literal.getSourceOperand()))
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.watches.value.function", literal.getSourceOperand().getName(), fieldName));
                }
                else
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.watches.value", fieldName, literalString));
                }
            }
        };
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
