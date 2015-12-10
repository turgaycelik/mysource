package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.plugin.jql.function.CascadeOptionFunction;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer}
 * for cascading select custom fields.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class CascadingSelectCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final ClauseNames clauseNames;
    private final CustomField customField;
    private final SelectConverter selectConverter;
    private final JqlOperandResolver jqlOperandResolver;
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;
    private final QueryContextConverter queryContextConverter;

    public CascadingSelectCustomFieldSearchInputTransformer(final ClauseNames clauseNames, final CustomField field, final String urlParameterName,
            final SelectConverter selectConverter, final JqlOperandResolver jqlOperandResolver,
            final JqlSelectOptionsUtil jqlSelectOptionsUtil, final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil,
            final QueryContextConverter queryContextConverter, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(field, urlParameterName, customFieldInputHelper);
        this.queryContextConverter = notNull("queryContextConverter", queryContextConverter);
        this.jqlCascadingSelectLiteralUtil = notNull("jqlCascadingSelectLiteralUtil", jqlCascadingSelectLiteralUtil);
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.customField = notNull("field", field);
        this.selectConverter = notNull("selectConverter", selectConverter);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return getParamsFromSearchRequest(user, query, searchContext) != null;
    }

    protected Clause getClauseFromParams(final User user, final CustomFieldParams customFieldParams)
    {
        Long childValue = null;
        Long parentValue = null;
        final String childStringValue = getValue(customFieldParams.getValuesForKey(CascadingSelectCFType.CHILD_KEY));
        final String parentStringValue = getValue(customFieldParams.getValuesForKey(CascadingSelectCFType.PARENT_KEY));
        try
        {
            if (childStringValue != null)
            {
                childValue = new Long(childStringValue);
            }

            if (parentStringValue != null)
            {
                parentValue = new Long(parentStringValue);
            }
        }
        catch (NumberFormatException e)
        {
            // invalid inputs - we will use the string values instead to build our clause
        }

        // child id is more specific than parent, so use it first
        final List<String> functionArgs = new ArrayList<String>();
        String invalidStringOperand = null;
        Long invalidLongOperand = null;
        if (childValue != null)
        {
            final Option childOption = jqlSelectOptionsUtil.getOptionById(childValue);
            if (childOption != null && childOption.getParentOption() != null)
            {
                functionArgs.add(childOption.getParentOption().getOptionId().toString());
                functionArgs.add(childOption.getOptionId().toString());
            }
            else
            {
                invalidLongOperand = childValue;
            }
        }
        else if (parentValue != null)
        {
            final Option parentOption = jqlSelectOptionsUtil.getOptionById(parentValue);
            if (parentOption != null)
            {
                functionArgs.add(parentOption.getOptionId().toString());
            }
            else
            {
                invalidLongOperand = parentValue;
            }
        }
        else if (childStringValue != null)
        {
            invalidStringOperand = childStringValue;
        }
        else if (parentStringValue != null)
        {
            invalidStringOperand = parentStringValue;
        }

        final String clauseName = getClauseName(user, clauseNames);
        if (invalidStringOperand != null || invalidLongOperand != null)
        {
            final Operand o = invalidStringOperand != null ? new SingleValueOperand(invalidStringOperand) : new SingleValueOperand(invalidLongOperand);
            return new TerminalClauseImpl(clauseName, Operator.EQUALS, o);
        }
        else if (!functionArgs.isEmpty())
        {
            return new TerminalClauseImpl(clauseName, Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, functionArgs));
        }
        else
        {
            return null;
        }
    }

    protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            SimpleNavigatorCollectorVisitor visitor = createSimpleNavigatorCollectingVisitor();
            query.getWhereClause().accept(visitor);

            // check that the structure is valid
            if (!visitor.isValid())
            {
                return null;
            }
            final List<TerminalClause> clauses = visitor.getClauses();

            // check that we only have one clause
            if (clauses.size() != 1)
            {
                return null;
            }

            final TerminalClause clause = clauses.get(0);

            // check that we have a valid operator
            final Operator operator = clause.getOperator();
            if (operator != Operator.EQUALS && operator != Operator.IS && operator != Operator.IN)
            {
                return null;
            }

            // check that a single value is resolved and that it is non-negative (no means to represent negative search)
            final List<QueryLiteral> literals = jqlOperandResolver.getValues(user, clause.getOperand(), clause);
            if (literals == null || literals.size() != 1 || jqlCascadingSelectLiteralUtil.isNegativeLiteral(literals.get(0)))
            {
                if (clause.getOperand() instanceof FunctionOperand)
                {
                    return handleInvalidFunctionOperand(clause);
                } else {
                    return null;
                }
            }

            // check that we are searching for non-empty value
            final QueryLiteral literal = literals.get(0);
            if (literal.isEmpty())
            {
                return null;
            }

            // check that the options resolved are in context
            final QueryContext queryContext = queryContextConverter.getQueryContext(searchContext);
            List<Option> options = jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true);

            if (options.size() > 1)
            {
                return null;
            }

            CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);

            if (options.size() == 0)
            {
                customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton(literal.asString()));
            }
            else
            {
                final Option option = options.get(0);
                final Option parentOption = option.getParentOption();

                if (parentOption != null)
                {
                    customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton(parentOption.getOptionId().toString()));
                    customFieldParams.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton(option.getOptionId().toString()));
                }
                else
                {
                    customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton(option.getOptionId().toString()));
                }
            }
            return customFieldParams;
        }

        return null;
    }

    private CustomFieldParams handleInvalidFunctionOperand(final TerminalClause clause)
    {
        CustomFieldParams customFieldParams = null;
        FunctionOperand fop = (FunctionOperand)clause.getOperand();
        if (fop.getName().equals(CascadeOptionFunction.FUNCTION_CASCADE_OPTION))
        {
            if (fop.getArgs().size() == 2)
            {
                customFieldParams = new CustomFieldParamsImpl(customField);
                customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton(fop.getArgs().get(0)));
                customFieldParams.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton(fop.getArgs().get(1)));
            }
            else if (fop.getArgs().size() == 1)
            {
                customFieldParams = new CustomFieldParamsImpl(customField);
                customFieldParams.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton(fop.getArgs().get(0)));
            }
            if (customFieldParams != null) {
                String parentValue = (String) customFieldParams.getFirstValueForKey(CascadingSelectCFType.PARENT_KEY);
                final List<Option> options = jqlSelectOptionsUtil.getOptions(customField, new QueryLiteral(clause.getOperand(), parentValue), true);
                if (options.isEmpty() || options.get(0) == null)
                {
                    return null;
                }
            }
        }
        return customFieldParams;
    }

    private String getValue(final Collection values) throws NumberFormatException
    {
        if (values == null || values.isEmpty())
        {
            return null;
        }
        String stringValue = (String) values.iterator().next();
        if ("".equals(stringValue) || "-1".equals(stringValue) || "-2".equals(stringValue))
        {
            return null;
        }
        return stringValue;
    }

    ///CLOVER:OFF
    SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
    {
        return new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
    }
    ///CLOVER:ON
}
