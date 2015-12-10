package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.project.ProjectConstant;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

abstract class AbstractProjectConstantsSearchInputTransformer<T extends ProjectConstant, I extends SearchInput> implements SearchInputTransformer
{
    private final String urlParameterName;
    protected final ClauseNames clauseNames;
    protected final JqlOperandResolver operandResolver;
    protected final NameResolver<T> nameResolver;
    private final NavigatorStructureChecker<T> navigatorStructureChecker;

    @VisibleForTesting
    AbstractProjectConstantsSearchInputTransformer(ClauseNames clauseNames, String urlParameterName,
            JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry,
            NameResolver<T> nameResolver)
    {
        this(clauseNames, urlParameterName, operandResolver, nameResolver,
                new NavigatorStructureChecker<T>(clauseNames, true, fieldFlagOperandRegistry, operandResolver));}

    AbstractProjectConstantsSearchInputTransformer(ClauseNames clauseNames, String urlParameterName,
            JqlOperandResolver operandResolver, NameResolver<T> nameResolver, final NavigatorStructureChecker<T> checker)
    {
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.urlParameterName = urlParameterName;
        this.operandResolver = operandResolver;
        this.nameResolver = nameResolver;
        this.navigatorStructureChecker = checker;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void populateFromParams(User user, FieldValuesHolder fieldValuesHolder, ActionParams actionParams)
    {
        String[] params = actionParams.getValuesForKey(urlParameterName);
        Set<I> values = Sets.newLinkedHashSet();

        if (params != null)
        {
            for (String param : params)
            {
                String[] parts = param.split(":", 2);
                values.add(parseInputParam(parts));
            }
        }

        fieldValuesHolder.put(urlParameterName, values);
    }

    @Override
    public void validateParams(User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, I18nHelper i18nHelper, ErrorCollection errors)
    {
        // No validation
    }

    @Override
    @SuppressWarnings("unchecked")
    public void populateFromQuery(User user, FieldValuesHolder fieldValuesHolder, Query query, SearchContext searchContext)
    {
        List<TerminalClause> clauses = getMatchingClauses(clauseNames.getJqlFieldNames(), query);
        Set<I> values = Sets.newLinkedHashSet();
        for (TerminalClause clause : clauses)
        {
            parseOperand(clause.getOperand(), values);
        }

        fieldValuesHolder.put(urlParameterName, values);
    }

    private List<TerminalClause> getMatchingClauses(final Set<String> jqlClauseNames, final Query query)
    {
        NamedTerminalClauseCollectingVisitor clauseVisitor = new NamedTerminalClauseCollectingVisitor(jqlClauseNames);
        if(query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(clauseVisitor);
            return clauseVisitor.getNamedClauses();
        }
        return Collections.emptyList();
    }

    private void parseOperand(Operand operand, Set<I> values)
    {
        if (operand instanceof EmptyOperand)
        {
            values.add(noValueInput());
        }
        else if (operand instanceof SingleValueOperand)
        {
            parseSingleValueOperand((SingleValueOperand) operand, values);
        }
        else if (operand instanceof FunctionOperand)
        {
            parseFunctionOperand((FunctionOperand) operand, values);
        }
        else if (operand instanceof MultiValueOperand)
        {
            MultiValueOperand multiValueOperand = (MultiValueOperand) operand;
            for (Operand value : multiValueOperand.getValues())
            {
                parseOperand(value, values);
            }
        }
    }

    private void parseSingleValueOperand(SingleValueOperand operand, Set<I> values)
    {
        String value = null;

        if (operand.getStringValue() != null)
        {
            try
            {
                Long id = Long.valueOf(operand.getStringValue());
                T object = findById(id);
                if (object != null)
                {
                    value = object.getName();
                    if (nameResolver.nameExists(operand.getStringValue()))
                    {
                        values.add(inputValue(operand.getStringValue()));
                    }
                }
                else
                {
                    value = operand.getStringValue();
                }
            }
            catch (NumberFormatException e)
            {
                value = operand.getStringValue();
            }
        }
        else if (operand.getLongValue() != null)
        {
            T object = findById(operand.getLongValue());
            if (object != null)
            {
                value = object.getName();
            }
            else
            {
                value = String.valueOf(operand.getLongValue());
            }
        }

        values.add(inputValue(value));
    }

    public Clause getSearchClause(User user, FieldValuesHolder fieldValuesHolder)
    {
        Collection<Operand> operands = Lists.newArrayList();

        @SuppressWarnings("unchecked")
        Collection<I> values = (Collection<I>) fieldValuesHolder.get(urlParameterName);

        boolean containsList = false;

        if (values != null)
        {
            for (I value : values)
            {
                Operand operand = parseInputValue(value);
                operands.add(operand);

                containsList = operandResolver.isListOperand(operand);
            }
        }

        if (operands.isEmpty())
        {
            return null;
        }

        if (operands.size() == 1)
        {
            return new TerminalClauseImpl(getClauseName(user, clauseNames), containsList ? Operator.IN : Operator.EQUALS, operands.iterator().next());
        }

        return new TerminalClauseImpl(getClauseName(user, clauseNames), Operator.IN, new MultiValueOperand(operands));
    }

    protected String getClauseName(final User user, final ClauseNames clauseNames)
    {
        return clauseNames.getPrimaryName();
    }

    @Override
    public boolean doRelevantClausesFitFilterForm(User searcher, Query query, SearchContext searchContext)
    {
        return navigatorStructureChecker.checkSearchRequest(query) && checkValidValues(searcher, query);
    }

    private boolean checkValidValues(User searcher, Query query)
    {
        SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
        if (query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(collector);
            if (collector.getClauses().size() == 1)
            {
                TerminalClause terminalClause = collector.getClauses().get(0);
                Operand operand = terminalClause.getOperand();
                return checkValidValues(searcher, operand, terminalClause);
            }
        }

        return true;
    }

    private boolean checkValidValues(User searcher, Operand operand, TerminalClause terminalClause)
    {
        if (operandResolver.isFunctionOperand(operand) || operandResolver.isEmptyOperand(operand))
        {
            return true;
        }
        else if (operandResolver.isListOperand(operand))
        {
            MultiValueOperand listOperand = (MultiValueOperand) operand;

            for (Operand childOperand : listOperand.getValues())
            {
                if (!checkValidValues(searcher, childOperand, terminalClause))
                {
                    return false;
                }
            }

            return true;
        }

        // SingleValueOperand
        final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);
        for (QueryLiteral value : values)
        {
            if (!checkClauseValuesForBasic(value))
            {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private T findById(Long id)
    {
        return nameResolver.get(id);
    }

    /**
     * Parse the URL parameter into objects into instances of {@link SearchInput}
     *
     * @param parts the URL parameter split over the first colon.
     *   For example, id:10292 will be passed as ["id", "10292"].
     * @return The {@link SearchInput} parsed from the parameters.
     */
    @Nonnull
    abstract I parseInputParam(String[] parts);

    /**
     * Turn the passed string into instances of {@link SearchInput}.
     *
     * @param value the value to transform.
     * @return The {@link SearchInput} generated from the passed value.
     */
    @Nonnull
    abstract I inputValue(String value);

    /**
     * Return the {@link SearchInput} used to represent the empty JQL search.
     * @return the {@link SearchInput} used to represent the empty JQL search.
     */
    @Nonnull
    abstract I noValueInput();

    /**
     * Convert the passed function into {@link SearchInput} values.
     *
     * @param operand the function to convert.
     * @param values collection to add all relevant {@link SearchInput}s to.
     */
    abstract void parseFunctionOperand(FunctionOperand operand, Set<I> values);

    /**
     * Convert the passed {@link SearchInput} into an equilavent {@link Operand} that can be used in a JQL query.
     * @param value the {@link SearchInput} to transform.
     * @return the converted value.
     */
    @Nonnull
    abstract Operand parseInputValue(I value);

    /**
     * Check to see that the passed {@link QueryLiteral} is valid for the basic issue navigator.
     *
     * @param literal the literal to check.
     * @return true iff the literal is valid and can be displayed in the basic navigator.
     */
    abstract boolean checkClauseValuesForBasic(QueryLiteral literal);
}
