package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.AllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Builds lucene queries for the "all text" clause. This clause aggregates all the free text fields in the system which
 * the user can see (or simply all fields if we are overriding security). Therefore, we acquire the appropriate clause
 * query factories for these fields, collect their individual results, then aggregate them with SHOULDs.
 * <p>
 * For example, the query <code>text ~ "john"</code> is equivalent to <code>summary ~ "john" OR description ~ "john" OR ...</code>.
 *
 * @see com.atlassian.jira.issue.search.constants.AllTextSearchConstants
 * @since v4.0
 */
public class AllTextClauseQueryFactory implements ClauseQueryFactory
{
    private final CustomFieldManager customFieldManager;
    private final SearchHandlerManager searchHandlerManager;

    public AllTextClauseQueryFactory(final CustomFieldManager customFieldManager, final SearchHandlerManager searchHandlerManager)
    {
        this.customFieldManager = notNull("customFieldManager", customFieldManager);
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();

        if (operator != Operator.LIKE)
        {
            return QueryFactoryResult.createFalseResult();
        }

        final List<ClauseQueryFactory> factories = getFactories(queryCreationContext);
        final List<QueryFactoryResult> results = new ArrayList<QueryFactoryResult>(factories.size());
        for (ClauseQueryFactory clauseQueryFactory : factories)
        {
            results.add(clauseQueryFactory.getQuery(queryCreationContext, terminalClause));
        }

        return QueryFactoryResult.mergeResultsWithShould(results); // TODO CJM kickass changes this
    }

    List<ClauseQueryFactory> getFactories(final QueryCreationContext queryCreationContext)
    {
        final CollectionBuilder<ClauseQueryFactory> factoryCollectionBuilder = CollectionBuilder.newBuilder();

        factoryCollectionBuilder.addAll(getAllSystemFieldFactories(queryCreationContext));
        factoryCollectionBuilder.addAll(getAllCustomFieldFactories(queryCreationContext));

        return factoryCollectionBuilder.asList();
    }

    List<ClauseQueryFactory> getAllSystemFieldFactories(final QueryCreationContext queryCreationContext)
    {
        final List<ClauseQueryFactory> factories = new ArrayList<ClauseQueryFactory>();
        final List<String> systemFieldClauseNames = CollectionBuilder.newBuilder(
                SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()
        ).asList();

        for (String clauseName : systemFieldClauseNames)
        {
            final Collection<ClauseHandler> handlers = getHandlersForClauseName(queryCreationContext, clauseName);
            for (ClauseHandler handler : handlers)
            {
                factories.add(handler.getFactory());
            }
        }

        return factories;
    }

    List<ClauseQueryFactory> getAllCustomFieldFactories(final QueryCreationContext queryCreationContext)
    {
        final List<ClauseQueryFactory> factories = new ArrayList<ClauseQueryFactory>();
        final List<CustomField> allCustomFields = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : allCustomFields)
        {
            final CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
            if (searcher == null)
            {
                continue;
            }

            final CustomFieldSearcherClauseHandler fieldSearcherClauseHandler = searcher.getCustomFieldSearcherClauseHandler();

            if (fieldSearcherClauseHandler == null || !(fieldSearcherClauseHandler instanceof AllTextCustomFieldSearcherClauseHandler))
            {
                continue;
            }

            if (!fieldSearcherClauseHandler.getSupportedOperators().contains(Operator.LIKE))
            {
                continue;
            }

            final Collection<ClauseHandler> handlers = getHandlersForClauseName(queryCreationContext, customField.getClauseNames().getPrimaryName());
            for (ClauseHandler handler : handlers)
            {
                factories.add(handler.getFactory());
            }
        }
        return factories;
    }

    private Collection<ClauseHandler> getHandlersForClauseName(final QueryCreationContext queryCreationContext, final String primaryClauseName)
    {
        if (queryCreationContext.isSecurityOverriden())
        {
            return searchHandlerManager.getClauseHandler(primaryClauseName);
        }
        else
        {
            return searchHandlerManager.getClauseHandler(queryCreationContext.getUser(), primaryClauseName);
        }
    }
}
