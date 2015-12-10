package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.ProjectCategoryResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A validator that checks to see if a project category exists.
 *
 * @since v4.0
 */
@InjectableComponent
public class ProjectCategoryValidator implements ClauseValidator
{
    private final ProjectCategoryResolver projectCategoryResolver;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlOperandResolver jqlOperandResolver;

    public ProjectCategoryValidator(final ProjectCategoryResolver projectCategoryResolver, final JqlOperandResolver jqlOperandResolver)
    {
        this.projectCategoryResolver = notNull("projectCategoryResolver", projectCategoryResolver);
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
                // Now lets look up the project category and see if it exists
                final I18nHelper i18n = getI18n(searcher);

                for (QueryLiteral rawValue : rawValues)
                {
                    // empty literals are always valid
                    if (rawValue.isEmpty())
                    {
                        continue;
                    }

                    // Get a project category for each individual raw value and see if it resolves
                    final GenericValue projectCategory = projectCategoryResolver.getProjectCategory(rawValue);
                    if (projectCategory == null)
                    {
                        if (rawValue.getStringValue() != null)
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
                        else if (rawValue.getLongValue() != null)
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
