package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The context calculation for cascading select. This context factory is slightly inconsistent from others in JIRA.
 * In other fields it is true that context(field in (a, c)) = context(field = a or field = c). We decided not to
 * enforce this for the cascading select because it uses encoded query literals to represent a single query. For example,
 * the query "field in cascadeoption(p1, NONE)" will actually return  "QueryLiteral(id(p1))", "QueryLiteral(-id(p1child1))",
 * "QueryLiteral(-id(p1child2))",...,"QueryLiteral(-id(p1childn))". In this case we look at every literal when calculating
 * the context rather than at each literal indivuidally so that we can produce a some useful context.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class CascadingSelectCustomFieldClauseContextFactory implements ClauseContextFactory
{
    private final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;
    private final JqlOperandResolver jqlOperandResolver;
    private final CustomField customField;
    private final ContextSetUtil contextSetUtil;
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    private final OperatorUsageValidator operatorUsageValidator;

    public CascadingSelectCustomFieldClauseContextFactory(final CustomField customField, final ContextSetUtil contextSetUtil,
            final JqlSelectOptionsUtil jqlSelectOptionsUtil, final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil,
            final JqlOperandResolver jqlOperandResolver, final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil,
            final OperatorUsageValidator operatorUsageValidator)
    {
        this.operatorUsageValidator = notNull("operatorUsageValidator", operatorUsageValidator);
        this.customField = notNull("customField", customField);
        this.contextSetUtil = notNull("contextSetUtil", contextSetUtil);
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
        this.fieldConfigSchemeClauseContextUtil = notNull("fieldConfigSchemeClauseContextUtil", fieldConfigSchemeClauseContextUtil);
        this.jqlCascadingSelectLiteralUtil = notNull("jqlCascadingSelectLiteralUtil", jqlCascadingSelectLiteralUtil);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final List<FieldConfigScheme> fieldConfigSchemes = customField.getConfigurationSchemes();
        if (fieldConfigSchemes == null || fieldConfigSchemes.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }

        final Set<Option> positiveOps = new HashSet<Option>();
        final Set<Option> negativeOps = new HashSet<Option>();
        final Operator operator = terminalClause.getOperator();
        final boolean positiveOperator = isPositiveOperator(operator);
        final boolean includeAll;

        if (handlesOperator(operator) && operatorUsageValidator.check(searcher, terminalClause))
        {
            fillOptions(searcher, terminalClause, positiveOps, negativeOps);

            //Remove the empty literals. They don't count towards context.
            negativeOps.remove(null);
            positiveOps.remove(null);

            if (positiveOperator)
            {
                //Having no positive queries means we will return nothing. To stop this we basically return all
                //contexts for the field.
                includeAll = positiveOps.isEmpty();
            }
            else
            {
                //In this case we have nothing to match against, so lets return all possible contexts for the field.
                //Will only happen on an error.
                includeAll = positiveOps.isEmpty() && negativeOps.isEmpty();
            }

        }
        else
        {
            includeAll = true;
        }

        final Set<ClauseContext> contexts = new HashSet<ClauseContext>();
        for (FieldConfigScheme fieldConfigScheme : fieldConfigSchemes)
        {
            // if we only have negative values, the resultant context is the union of the contexts of all fieldconfigschemes
            // also, the empty value is part of every configuration of this custom field
            if (includeAll || matchesOptions(positiveOperator, fieldConfigScheme, positiveOps, negativeOps))
            {
                if (fieldConfigScheme.isGlobal())
                {
                    return ClauseContextImpl.createGlobalClauseContext();
                }
                else
                {
                    contexts.add(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(searcher, fieldConfigScheme));
                }
            }
        }

        if (contexts.isEmpty())
        {
            return ClauseContextImpl.createGlobalClauseContext();
        }
        else
        {
            final ClauseContext calcContext;
            if (contexts.size() == 1)
            {
                calcContext = contexts.iterator().next();
            }
            else
            {
                calcContext = contextSetUtil.union(contexts);
            }

            return calcContext.getContexts().isEmpty() ? ClauseContextImpl.createGlobalClauseContext() : calcContext;
        }
    }

    private boolean matchesOptions(final boolean positiveOperator, final FieldConfigScheme fieldConfigScheme, final Set<Option> positiveOps, final Set<Option> negativeOps)
    {
        if (positiveOperator)
        {
            return matchesOptionsPositive(fieldConfigScheme, positiveOps, negativeOps);
        }
        else
        {
            return matchesOptionsNegative(fieldConfigScheme, positiveOps, negativeOps);
        }
    }

    //
    // In the negative case we are doing a query of the form (-positiveOps) (negativeOps), that is, we wont match any
    // of the positive options provided they are not listed in the negative operations. This can occur with the
    // query "select not in cascadeoption(p1, NONE)" where we should match all issues that don't have (p1, NONE) selected
    // as an option.
    //
    private boolean matchesOptionsNegative(final FieldConfigScheme fieldConfigScheme, final Set<Option> positiveOps, final Set<Option> negativeOps)
    {
        final Set<Option> options = new HashSet<Option>(jqlSelectOptionsUtil.getOptionsForScheme(fieldConfigScheme));
        if (CollectionUtils.containsAny(options, negativeOps))
        {
            return true;
        }
        else
        {
            removeOptions(options, positiveOps);
            return !options.isEmpty();
        }
    }

    //
    // In the positive case we are doing a query of the form +(positiveOps) +(-negativeOps), that is, match any of the
    // positive options provided they are not in the negative options. This can occur if we only want to match a parent
    // option (+ve) without matching any of its children (-ve). For example, the query "select in cascadeoption(p1, NONE)"
    // will have positiveOps = {p1} and negativeOps = {all children of p1}.
    //
    private boolean matchesOptionsPositive(final FieldConfigScheme fieldConfigScheme, final Set<Option> positiveOps, final Set<Option> negativeOps)
    {
        if (positiveOps.isEmpty())
        {
            return false;
        }

        final Set<Option> options = new HashSet<Option>(jqlSelectOptionsUtil.getOptionsForScheme(fieldConfigScheme));

        //Note that if we can't match a parent, then we can't match any of its children.
        removeOptions(options, negativeOps);
        return CollectionUtils.containsAny(options, positiveOps);
    }

    private static void removeOptions(Collection<Option> options, Collection<Option> removeOptions)
    {
        for (Option remove : removeOptions)
        {
            options.remove(remove);
            final List<Option> list = remove.getChildOptions();
            if (list != null)
            {
                options.removeAll(list);
            }
        }
    }

    //
    // For testing.
    //
    void fillOptions(User user, TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
    {
        final List<QueryLiteral> literals = jqlOperandResolver.getValues(user, clause.getOperand(), clause);
        if (literals == null)
        {
            return;
        }

        final List<QueryLiteral> positiveLiterals = new ArrayList<QueryLiteral>();
        final List<QueryLiteral> negativeLiterals = new ArrayList<QueryLiteral>();

        jqlCascadingSelectLiteralUtil.processPositiveNegativeOptionLiterals(literals, positiveLiterals, negativeLiterals);
        fillOptions(positiveLiterals, positiveOption);
        fillOptions(negativeLiterals, negativeOption);
    }

    private void fillOptions(Collection<QueryLiteral> literal, Set<Option> options)
    {
        for (QueryLiteral queryLiteral : literal)
        {
            options.addAll(jqlSelectOptionsUtil.getOptions(customField, queryLiteral, true));
        }
    }

    private boolean isPositiveOperator(final Operator operator)
    {
        return Operator.EQUALS == operator || Operator.IN == operator || Operator.IS == operator;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
    }
}
