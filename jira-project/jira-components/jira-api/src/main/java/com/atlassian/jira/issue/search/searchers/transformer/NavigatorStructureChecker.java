package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Utility class to assist in checking the fitness of a query for the issue navigator.
 *
 * @since v4.0
 */
public class NavigatorStructureChecker<T>
{
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private final JqlOperandResolver operandResolver;
    private final boolean supportMultiLevelFunctions;
    private final ClauseNames clauseNames;

    /**
     *
     * @param clauseNames The clause field names of the clause to check.
     * @param supportMultiLevelFunctions set to true if the clause type supports functions inside of multivalue operands
     * @param fieldFlagOperandRegistry the FieldFlagOperandRegistry
     * @param operandResolver the JqlOperandSupport
     */
    public NavigatorStructureChecker(final ClauseNames clauseNames, boolean supportMultiLevelFunctions, final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final JqlOperandResolver operandResolver)
    {
        this.supportMultiLevelFunctions = supportMultiLevelFunctions;
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.fieldFlagOperandRegistry = notNull("fieldFlagOperandRegistry", fieldFlagOperandRegistry);
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    @Deprecated
    public NavigatorStructureChecker(final ClauseNames clauseNames, boolean supportMultiLevelFunctions, final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final JqlOperandResolver operandResolver, final IndexInfoResolver<T> indexInfoResolver,
            final SearchContextVisibilityChecker searchContextVisibilityChecker)
    {
        this(clauseNames, supportMultiLevelFunctions, fieldFlagOperandRegistry, operandResolver);
    }

    /**
     * Checks that a {@link SearchRequest}'s query conforms to the simple structure required to fit in the Issue
     * Navigator.
     *
     * @param query the search criteria used to populate the field values holder.
     * @param searchContext the context under which the search is being performed.
     * @return true if it will fit; false otherwise.
     * @deprecated Since 6.3.4. The {@link com.atlassian.jira.issue.search.SearchContext} parameter is no longer needed. Use {@link #checkSearchRequest(com.atlassian.query.Query)}
     */
    @Deprecated
    public boolean checkSearchRequest(Query query, SearchContext searchContext)
    {
        return checkSearchRequest(query);
    }

    /**
     * Checks that a {@link SearchRequest}'s query conforms to the simple structure required to fit in the Issue Navigator.
     *
     * @param query the search criteria used to populate the field values holder.
     * @return true if it will fit; false otherwise.
     */
    public boolean checkSearchRequest(Query query)
    {
        if (query != null && query.getWhereClause() != null)
        {
            final Clause whereClause = query.getWhereClause();
            final SimpleNavigatorCollectorVisitor collector = createSimpleNavigatorCollectorVisitor();
            whereClause.accept(collector);
            if (!collector.isValid() || collector.getClauses().size() > 1)
            {
                return false;
            }
            else if (collector.getClauses().size() == 1)
            {
                final TerminalClause terminalClause = collector.getClauses().get(0);
                return checkOperator(terminalClause.getOperator()) && checkOperand(terminalClause.getOperand(), true);
            }
        }
        return true;
    }

    boolean checkOperator(final Operator operator)
    {
        switch (operator)
        {
            case EQUALS:
            case IN:
            case IS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks the validity of an {@link com.atlassian.query.operand.Operand} for this field in the issue navigator. Note
     * that it is assumed that the operand has already passed basic verification i.e. its values are correct for the
     * operator.
     *
     * @param operand the operand to check
     * @param acceptFunctions if true, {@link com.atlassian.query.operand.FunctionOperand}s will be accepted if it has
     * an associated flag in the {@link FieldFlagOperandRegistry}.
     * @return true if the operand was valid; false otherwise.
     */
    boolean checkOperand(Operand operand, boolean acceptFunctions)
    {
        if (isEmptyOperand(operand) || operand instanceof SingleValueOperand)
        {
            if (fieldFlagOperandRegistry.getFlagForOperand(clauseNames.getPrimaryName(), operand) != null)
            {
                return true;
            }
            else if (operand instanceof SingleValueOperand)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if (operand instanceof FunctionOperand)
        {
            // if this function represents a flag, then its okay
            return acceptFunctions && (fieldFlagOperandRegistry.getFlagForOperand(clauseNames.getPrimaryName(), operand) != null);
        }
        else if (operand instanceof MultiValueOperand)
        {
            boolean valid = true;
            for (Operand child : ((MultiValueOperand) operand).getValues())
            {
                if (!checkOperand(child, supportMultiLevelFunctions))
                {
                    valid = false;
                    break;
                }
            }
            return valid;
        }
        else
        {
            throw new IllegalArgumentException("Don't know how to validate operand '" + operand + "' for navigator");
        }
    }

    SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
    {
        return new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
    }

    private boolean isEmptyOperand(final Operand operand)
    {
        return operandResolver.isEmptyOperand(operand);
    }
}
