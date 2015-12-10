package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The default implementation for the {@link IndexedInputHelper}.
 * <p/>
 * This class should be constructed as needed and not injected, as the {@link IndexInfoResolver} is only relevant to
 * specific field(s).
 *
 * @since v4.0
 */
@NonInjectableComponent
public class DefaultIndexedInputHelper<T> implements IndexedInputHelper
{
    private static final Logger log = Logger.getLogger(DefaultIndexedInputHelper.class);

    private final JqlOperandResolver operandResolver;
    private final IndexInfoResolver<T> indexInfoResolver;
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;

    public DefaultIndexedInputHelper(IndexInfoResolver<T> indexInfoResolver, JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        this.operandResolver = operandResolver;
        this.indexInfoResolver = indexInfoResolver;
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
    }

    @Deprecated
    public DefaultIndexedInputHelper(IndexInfoResolver<T> indexInfoResolver, JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry, final SearchContextVisibilityChecker searchContextVisibilityChecker)
    {
        this(indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
    }

    public Set<String> getAllIndexValuesForMatchingClauses(final User searcher, final ClauseNames jqlClauseNames, final Query query, final SearchContext searchContext)
    {
        return getAllIndexValuesForMatchingClauses(searcher, jqlClauseNames, query);
    }

    public Set<String> getAllIndexValuesForMatchingClauses(final User searcher, final ClauseNames jqlClauseNames, final Query query)
    {
        Set<String> allValues = new LinkedHashSet<String>();
        List<TerminalClause> clauses = getMatchingClauses(jqlClauseNames.getJqlFieldNames(), query);
        for (TerminalClause clause : clauses)
        {
            allValues.addAll(getAllIndexValuesAsStrings(searcher, clause.getOperand(), clause));
        }
        return allValues;
    }

    public Set<String> getAllNavigatorValuesForMatchingClauses(final User searcher, ClauseNames jqlClauseNames, final Query query, final SearchContext searchContext)
    {
        return getAllNavigatorValuesForMatchingClauses(searcher, jqlClauseNames, query);
    }

    public Set<String> getAllNavigatorValuesForMatchingClauses(final User searcher, ClauseNames jqlClauseNames, final Query query)
    {
        Set<String> allValues = new LinkedHashSet<String>();
        List<TerminalClause> clauses = getMatchingClauses(jqlClauseNames.getJqlFieldNames(), query);
        for (TerminalClause clause : clauses)
        {
            allValues.addAll(getAllNavigatorValues(searcher, jqlClauseNames.getPrimaryName(), clause.getOperand(), clause));
        }
        return allValues;
    }

    public Clause getClauseForNavigatorValues(final String clauseName, final Set<String> values)
    {
        notNull("values", values);
        final Set<Operand> operandValues = Sets.newLinkedHashSet();
        boolean containsList = false;
        for (String stringValue : values)
        {
            // Note: need a Long because we need to ensure the Clause searches on id and not name.
            Operand operand = fieldFlagOperandRegistry.getOperandForFlag(clauseName, stringValue);
            if (operand != null)
            {
                operandValues.add(operand);
                containsList = operandResolver.isListOperand(operand);
            }
            else
            {
                operand = createOperand(stringValue);
                operandValues.add(operand);
                containsList = operandResolver.isListOperand(operand);
            }
        }

        if (operandValues.size() == 1)
        {
            return new TerminalClauseImpl(clauseName, containsList ? Operator.IN : Operator.EQUALS, operandValues.iterator().next());
        }
        else if (operandValues.size() > 1)
        {
            return new TerminalClauseImpl(clauseName, Operator.IN, new MultiValueOperand(operandValues));
        }
        else
        {
            return null;
        }
    }

    /**
     * Resolve this string representation of a navigator value (known not to be a field flag) into an operand to be used
     * in a clause. Override this to provide domain-specific resolution (e.g. resolve version ids to names). Default implementation
     * delegates to {@link #createSingleValueOperandFromId}
     *
     * @param stringValue the navigator value as a string e.g. <code>123</code>
     * @return the operand which best represents this navigator value - either a string name or the id or whatever.
     */
    protected Operand createOperand(String stringValue)
    {
        return createSingleValueOperandFromId(stringValue);
    }

    /**
     * Resolve this string representation of a navigator value (known not to be a field flag) into an operand to be used
     * in a clause (assuming that this is a single value operand). Override this to provide domain-specific resolution (e.g. resolve version ids to names).
     *
     * @param stringValue the navigator value as a string e.g. <code>123</code>
     * @return the operand which best represents this navigator value - either a string name or the id or whatever.
     */
    protected SingleValueOperand createSingleValueOperandFromId(final String stringValue)
    {
        // if Long doesn't parse, then assume bad input but create clause anyway
        SingleValueOperand o;
        try
        {
            o = new SingleValueOperand(new Long(stringValue));
        }
        catch (NumberFormatException e)
        {
            // we got some project id - we will fall back to using String as the operand
            if (log.isDebugEnabled())
            {
                log.debug("Got a strange non-id input '" + stringValue + "' - continuing anyway so that clause is still constructed.");
            }
            o = new SingleValueOperand(stringValue);
        }
        return o;
    }

    /*
     * This method is used to implement {@link IndexedInputHelper#getAllNavigatorValuesForMatchingClauses} and must follow
     * the contract as defined by {@link IndexedInputHelper#getAllNavigatorValues}.
     */
    Set<String> getAllNavigatorValues(final User searcher, final String fieldName, final Operand operand, final TerminalClause clause)
    {
        // if we have a way to represent this operand as a navigator-specific flag, do it
        final Set<String> flags = fieldFlagOperandRegistry.getFlagForOperand(fieldName, operand);
        if (flags != null)
        {
            return flags;
        }
        else if (operand instanceof MultiValueOperand)
        {
            MultiValueOperand multiValueOperand = (MultiValueOperand) operand;
            Set<String> values = new LinkedHashSet<String>();

            for (Operand subOperand : multiValueOperand.getValues())
            {
                values.addAll(getAllNavigatorValues(searcher, fieldName, subOperand, clause));
            }
            return values;
        }

        return getAllIndexValuesAsStrings(searcher, operand, clause);
    }

    private Set<String> getAllIndexValuesAsStrings(final User searcher, final Operand operand, final TerminalClause clause)
    {
        Set<String> allValues = new LinkedHashSet<String>();
        List<QueryLiteral> values = operandResolver.getValues(searcher, operand, clause);
        if (values != null)
        {
            for (QueryLiteral literal : values)
            {
                final List<String> idsAsStrings;
                if (literal.getStringValue() != null)
                {
                    idsAsStrings = indexInfoResolver.getIndexedValues(literal.getStringValue());
                }
                else if (literal.getLongValue() != null)
                {
                    idsAsStrings = indexInfoResolver.getIndexedValues(literal.getLongValue());
                }
                else
                {
                    // empty literal or something unexpected; ignore
                    continue;
                }

                if (idsAsStrings != null)
                {
                    allValues.addAll(idsAsStrings);
                }
            }
        }

        return allValues;
    }

    private List<TerminalClause> getMatchingClauses(final Set<String> jqlClauseNames, final Query query)
    {
        final NamedTerminalClauseCollectingVisitor clauseVisitor = new NamedTerminalClauseCollectingVisitor(jqlClauseNames);
        if(query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(clauseVisitor);
            return clauseVisitor.getNamedClauses();
        }
        return Collections.emptyList();
    }
}
