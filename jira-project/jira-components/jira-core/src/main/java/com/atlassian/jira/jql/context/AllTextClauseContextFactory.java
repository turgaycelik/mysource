package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.AllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Calculates the context of the "all text" clause. Since the clause essentially aggregates all the free text fields
 * visible to the user, the context is calculated by aggregating the contexts of each individual clause.
 *
 * @see com.atlassian.jira.jql.query.AllTextClauseQueryFactory
 * @since v4.0
 */
@InjectableComponent
public class AllTextClauseContextFactory implements ClauseContextFactory
{
    private final CustomFieldManager customFieldManager;
    private final SearchHandlerManager searchHandlerManager;
    private final ContextSetUtil contextSetUtil;

    public AllTextClauseContextFactory(final CustomFieldManager customFieldManager, final SearchHandlerManager searchHandlerManager, final ContextSetUtil contextSetUtil)
    {
        this.customFieldManager = notNull("customFieldManager", customFieldManager);
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
        this.contextSetUtil = notNull("contextSetUtil", contextSetUtil);
    }

    public ClauseContext getClauseContext(final User searcher, final TerminalClause terminalClause)
    {
        final List<ClauseContextFactory> clauseContextFactories = getFactories(searcher);
        final Set<ClauseContext> contexts = new LinkedHashSet<ClauseContext>();

        for (ClauseContextFactory factory : clauseContextFactories)
        {
            contexts.add(factory.getClauseContext(searcher, terminalClause));
        }

        return contextSetUtil.union(contexts);
    }

    List<ClauseContextFactory> getFactories(final User searcher)
    {
        final CollectionBuilder<ClauseContextFactory> factoryCollectionBuilder = CollectionBuilder.newBuilder();

        factoryCollectionBuilder.addAll(getAllSystemFieldFactories(searcher));
        factoryCollectionBuilder.addAll(getAllCustomFieldFactories(searcher));

        return factoryCollectionBuilder.asList();
    }

    List<ClauseContextFactory> getAllSystemFieldFactories(final User searcher)
    {
        final List<ClauseContextFactory> factories = new ArrayList<ClauseContextFactory>();
        final List<String> systemFieldClauseNames = CollectionBuilder.newBuilder(
                SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(),
                SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()
        ).asList();

        for (String clauseName : systemFieldClauseNames)
        {
            final Collection<ClauseHandler> handlers = searchHandlerManager.getClauseHandler(searcher, clauseName);
            for (ClauseHandler handler : handlers)
            {
                factories.add(handler.getClauseContextFactory());
            }
        }

        return factories;
    }

    List<ClauseContextFactory> getAllCustomFieldFactories(final User user)
    {
        final List<ClauseContextFactory> factories = new ArrayList<ClauseContextFactory>();
        final List<CustomField> allCustomFields = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : allCustomFields)
        {
            final CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
            if (searcher == null)
            {
                continue;
            }

            final CustomFieldSearcherClauseHandler fieldSearcherClauseHandler = searcher.getCustomFieldSearcherClauseHandler();

            if (fieldSearcherClauseHandler == null || !fieldSearcherClauseHandler.getSupportedOperators().contains(Operator.LIKE))
            {
                continue;
            }

            if (!(fieldSearcherClauseHandler instanceof AllTextCustomFieldSearcherClauseHandler))
            {
                continue;
            }
            
            final Collection<ClauseHandler> handlers = searchHandlerManager.getClauseHandler(user, customField.getClauseNames().getPrimaryName());
            for (ClauseHandler handler : handlers)
            {
                factories.add(handler.getClauseContextFactory());
            }
        }
        return factories;
    }
}
