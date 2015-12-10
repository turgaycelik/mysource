package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.StatusCategoryResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A validator that checks to see if a status category exists.
 *
 * @since v6.2
 */
@InjectableComponent
public class StatusCategoryValidator implements ClauseValidator
{

    private final StatusCategoryResolver statusCategoryResolver;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlOperandResolver jqlOperandResolver;

    public StatusCategoryValidator(final JqlOperandResolver jqlOperandResolver, StatusCategoryResolver statusCategoryResolver)
    {
        this.statusCategoryResolver = notNull("statusCategoryResolver", statusCategoryResolver);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        //note: categories are visible to everyone

        final MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!messageSet.hasAnyErrors())
        {
            final Operand operand = terminalClause.getOperand();
            final List<QueryLiteral> rawValues = jqlOperandResolver.getValues(searcher, operand, terminalClause);
            if (rawValues != null)
            {
                // Now lets look up the status category and see if it exists
                final I18nHelper i18n = getI18n(searcher);

                for (QueryLiteral rawValue : rawValues)
                {
                    // empty literals are always valid
                    if (rawValue.isEmpty())
                    {
                        continue;
                    }

                    // Get a status category for each individual raw value and see if it resolves
                    final Option<StatusCategory> statusCategory = statusCategoryResolver.getStatusCategory(rawValue);

                    if (!statusCategory.isDefined())
                    {
                        if (rawValue.getLongValue() != null)
                        {
                            if (jqlOperandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), terminalClause.getName()));
                            }
                            else
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.id", terminalClause.getName(), rawValue.toString()));
                            }
                        }
                        else if (rawValue.getStringValue() != null)
                        {
                            if (jqlOperandResolver.isFunctionOperand(rawValue.getSourceOperand()))
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name.from.function", rawValue.getSourceOperand().getName(), terminalClause.getName()));
                            }
                            else
                            {
                                messageSet.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name", terminalClause.getName(), rawValue.toString()));
                            }
                        }
                    }
                }
            }
        }
        return messageSet;
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
