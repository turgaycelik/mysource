package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generates the ClauseContext for a select custom field. This takes into account what context the custom field
 * has been configured against, whether the custom field is visible in the field configuration scheme,
 * if the user has permission to see the project that the field has been configured against, and if the selected value
 * if in the configuration scheme.
 *
 * @since v4.0
 */
public class SelectCustomFieldClauseContextFactory implements ClauseContextFactory
{
    private final JqlOperandResolver jqlOperandResolver;
    private final CustomField customField;
    private final ContextSetUtil contextSetUtil;
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    private final OperatorUsageValidator operatorUsageValidator;

    public SelectCustomFieldClauseContextFactory(final CustomField customField, final ContextSetUtil contextSetUtil,
            final JqlSelectOptionsUtil jqlSelectOptionsUtil, final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil,
            final JqlOperandResolver jqlOperandResolver, final OperatorUsageValidator operatorUsageValidator)
    {
        this.operatorUsageValidator = notNull("operatorUsageValidator", operatorUsageValidator);
        this.customField = notNull("customField", customField);
        this.contextSetUtil = notNull("contextSetUtil", contextSetUtil);
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
        this.fieldConfigSchemeClauseContextUtil = notNull("fieldConfigSchemeClauseContextUtil", fieldConfigSchemeClauseContextUtil);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    public final ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final List<FieldConfigScheme> fieldConfigSchemes = customField.getConfigurationSchemes();
        if (fieldConfigSchemes == null || fieldConfigSchemes.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final Operator operator = terminalClause.getOperator();
        final Set<Option> literalOptions;
        final boolean includeAll;

        if (handlesOperator(operator) && operatorUsageValidator.check(searcher, terminalClause))
        {
            literalOptions = getContextOptions(searcher, terminalClause);
            literalOptions.remove(null);
            includeAll = literalOptions.isEmpty();
        }
        else
        {
            literalOptions = Collections.emptySet();
            includeAll = true;
        }

        final boolean positiveOperator = isPositiveOperator(operator);
        ClauseContext context = new ClauseContextImpl();
        for (FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
        {
            // if we only have negative values, the resultant context is the union of the contexts of all fieldconfigschemes
            // also, the empty value is part of every configuration of this custom field
            if (includeAll || matchesOptions(positiveOperator, fieldConfigScheme, literalOptions))
            {
                if (fieldConfigScheme.isGlobal())
                {
                    return ClauseContextImpl.createGlobalClauseContext();
                }
                else
                {
                    final ClauseContext configContext = fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(searcher, fieldConfigScheme);
                    context = contextSetUtil.union(CollectionBuilder.newBuilder(context, configContext).asSet());
                }
            }
        }

        return context.getContexts().isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : context;
    }

    private Set<Option> getContextOptions(final User searcher, final TerminalClause terminalClause)
    {
        final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, terminalClause.getOperand(), terminalClause);
        if (literals != null && !literals.isEmpty())
        {
            final Set<Option> literalOptions = new LinkedHashSet<Option>();
            for (QueryLiteral literal : literals)
            {
                literalOptions.addAll(jqlSelectOptionsUtil.getOptions(customField, literal, false));
            }
            return literalOptions;
        }
        else
        {
            return Collections.emptySet();
        }
    }

    private boolean matchesOptions(final boolean positiveOperator, final FieldConfigScheme fieldConfigScheme, final Collection<Option> literalOptions)
    {
        if (positiveOperator)
        {
            return schemeContainsOptions(fieldConfigScheme, literalOptions);
        }
        else
        {
            return schemeContainsOtherOptions(fieldConfigScheme, literalOptions);
        }
    }

    private boolean schemeContainsOtherOptions(final FieldConfigScheme fieldConfigScheme, final Collection<Option> options)
    {
        final Collection<Option> allOptions = jqlSelectOptionsUtil.getOptionsForScheme(fieldConfigScheme);
        return !options.containsAll(allOptions);
    }

    private boolean schemeContainsOptions(final FieldConfigScheme fieldConfigScheme, final Collection<Option> options)
    {
        final Set<Option> schemeOptions = new HashSet<Option>(jqlSelectOptionsUtil.getOptionsForScheme(fieldConfigScheme));
        return CollectionUtils.containsAny(schemeOptions, options);
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }

    private boolean isPositiveOperator(final Operator operator)
    {
        return Operator.EQUALS == operator || Operator.IN == operator || Operator.IS == operator;
    }
}
