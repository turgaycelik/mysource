package com.atlassian.jira.bc.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.RelevantSearcherVisitor;
import com.atlassian.jira.issue.search.searchers.util.TerminalClauseCollectingVisitor;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.jql.context.QueryContextVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.parser.JqlParseErrorMessage;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.permission.ClauseSanitisingVisitor;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.jql.validator.OrderByValidator;
import com.atlassian.jira.jql.validator.ValidatorVisitor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.issue.IssueNavigatorConstants;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The default implementation of the {@link com.atlassian.jira.bc.issue.search.SearchService}.
 *
 * @since v4.0
 */
public class DefaultSearchService implements SearchService
{
    private static final Logger log = Logger.getLogger(DefaultSearchService.class);

    private final JqlQueryParser jqlQueryParser;
    private final JqlStringSupport jqlStringSupport;
    private final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory;
    private final SearchHandlerManager searchHandlerManager;
    private final QueryContextVisitor.QueryContextVisitorFactory queryContextVisitorFactory;
    private final QueryContextConverter queryContextConverter;
    // NOTE: this is a request level cache, not a long term persistent cache
    private final QueryCache queryCache;
    private final JqlOperandResolver jqlOperandResolver;
    private final OrderByValidator orderByValidator;
    private final SearchProvider searchProvider;
    private final I18nHelper.BeanFactory factory;

    public DefaultSearchService(final SearchHandlerManager searchHandlerManager, final JqlQueryParser jqlQueryParser,
            final ValidatorVisitor.ValidatorVisitorFactory validatorVisitorFactory,
            final QueryContextVisitor.QueryContextVisitorFactory queryContextVisitorFactory, final JqlStringSupport jqlStringSupport,
            final QueryContextConverter queryContextConverter, final QueryCache queryCache, final JqlOperandResolver jqlOperandResolver,
            final OrderByValidator orderByValidator, final SearchProvider searchProvider, final I18nHelper.BeanFactory factory)
    {
        this.factory = factory;
        this.searchProvider = notNull("searchProvider", searchProvider);
        this.queryCache = notNull("queryCache", queryCache);
        this.queryContextConverter = notNull("queryContextConverter", queryContextConverter);
        this.queryContextVisitorFactory = notNull("queryContextVisitorFactory", queryContextVisitorFactory);
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
        this.jqlQueryParser = notNull("jqlQueryParser", jqlQueryParser);
        this.jqlStringSupport = notNull("jqlStringSupport", jqlStringSupport);
        this.validatorVisitorFactory = notNull("validatorVisitorFactory", validatorVisitorFactory);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.orderByValidator = notNull("orderByValidator", orderByValidator);
    }

    @Override
    public boolean doesQueryFitFilterForm(final User user, final Query query)
    {
        notNull("query", query);
        // This if check is a special case, the empty query and we do not need it to participate in the caching
        if (query.getWhereClause() != null)
        {
            Boolean doesItFit = queryCache.getDoesQueryFitFilterFormCache(user, query);
            if (doesItFit == null)
            {
                doesItFit = calculateDoesQueryFitFilterForm(user, query);
                queryCache.setDoesQueryFitFilterFormCache(user, query, doesItFit);
            }
            return doesItFit;
        }
        return true;
    }

    @Override
    public SearchResults search(final User searcher, final Query query, final PagerFilter pager) throws SearchException
    {
        return searchProvider.search(query, searcher, pager);
    }

    @Override
    public long searchCount(final User searcher, final Query query) throws SearchException
    {
        return searchProvider.searchCount(query, searcher);
    }

    @Override
    public SearchContext getSearchContext(User searcher, Query query)
    {
        if (query != null)
        {
            final QueryContext queryContext = getSimpleQueryContext(searcher, query);
            if (queryContext != null)
            {
                final SearchContext searchContext = queryContextConverter.getSearchContext(queryContext);
                if (searchContext != null)
                {
                    return searchContext;
                }
            }
        }

        // Could not generate one so lets return an empty one.
        return createSearchContext(Collections.<Long>emptyList(), Collections.<String>emptyList());
    }

    @Override
    public String getQueryString(final User user, final Query query)
    {
        final UrlBuilder url = createUrlBuilder();
        url.addParameter(IssueNavigatorConstants.JQL_QUERY_PARAMETER, getJqlString(query));
        return url.asUrlString();
    }

    @Override
    public ParseResult parseQuery(User searcher, final String query)
    {
        notNull("query", query);

        Query newQuery = null;
        MessageSet errors = new MessageSetImpl();
        try
        {
            newQuery = jqlQueryParser.parseQuery(query);
        }
        catch (JqlParseException exception)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to parse query.", exception);
            }

            JqlParseErrorMessage errorMessage = exception.getParseErrorMessage();
            if (errorMessage == null)
            {
                errorMessage = JqlParseErrorMessages.genericParseError();
            }

            errors.addErrorMessage(errorMessage.getLocalizedErrorMessage(getI18n(searcher)));
        }
        return new ParseResult(newQuery, errors);
    }

    @Override
    public QueryContext getQueryContext(User searcher, Query query)
    {
        notNull("query", query);

        // We know that the read and put are non-atomic but we will always generate the same result and it does not
        // matter if two threads over-write the result into the cache
        final Clause clause = query.getWhereClause();
        if (clause == null)
        {
            // return the ALL-ALL context for the all query
            return new QueryContextImpl(ClauseContextImpl.createGlobalClauseContext());
        }
        else
        {
            QueryContext queryContext = queryCache.getQueryContextCache(searcher, query);
            if (queryContext == null)
            {
                // calculate both the full and simple contexts and cache them
                final QueryContextVisitor visitor = queryContextVisitorFactory.createVisitor(searcher);
                final QueryContextVisitor.ContextResult result = clause.accept(visitor);
                queryContext = new QueryContextImpl(result.getFullContext());
                QueryContext explicitQueryContext = new QueryContextImpl(result.getSimpleContext());
                queryCache.setQueryContextCache(searcher, query, queryContext);
                queryCache.setSimpleQueryContextCache(searcher, query, explicitQueryContext);
            }
            return queryContext;
        }
    }

    @Override
    public QueryContext getSimpleQueryContext(User searcher, Query query)
    {
        notNull("query", query);

        // We know that the read and put are non-atomic but we will always generate the same result and it does not
        // matter if two threads over-write the result into the cache
        final Clause clause = query.getWhereClause();
        if (clause == null)
        {
            // return the ALL-ALL context for the all query
            return new QueryContextImpl(ClauseContextImpl.createGlobalClauseContext());
        }
        else
        {
            QueryContext simpleQueryContext = queryCache.getSimpleQueryContextCache(searcher, query);
            if (simpleQueryContext == null)
            {
                // calculate both the full and simple contexts and cache them again
                final QueryContextVisitor visitor = queryContextVisitorFactory.createVisitor(searcher);
                final QueryContextVisitor.ContextResult result = clause.accept(visitor);
                simpleQueryContext = new QueryContextImpl(result.getSimpleContext());
                QueryContext fullQueryContext = new QueryContextImpl(result.getFullContext());
                queryCache.setQueryContextCache(searcher, query, fullQueryContext);
                queryCache.setSimpleQueryContextCache(searcher, query, simpleQueryContext);
            }
            return simpleQueryContext;
        }
    }

    @Nonnull
    @Override
    public MessageSet validateQuery(final User searcher, @Nonnull final Query query)
    {
        return validateQuery(searcher, query, null);
    }

    @Nonnull
    @Override
    public MessageSet validateQuery(final User searcher, @Nonnull final Query query, final Long searchRequestId)
    {
        notNull("query", query);
        UtilTimerStack.push("DefaultSearchService.validateQuery()");
        try
        {
            final Clause clause = query.getWhereClause();

            final MessageSet messageSet;

            if (clause != null)
            {
                //Validate clause.
                final ValidatorVisitor visitor = validatorVisitorFactory.createVisitor(searcher, searchRequestId);
                messageSet = clause.accept(visitor);
            }
            else
            {
                messageSet = new MessageSetImpl();
            }

            final OrderBy orderBy = query.getOrderByClause();
            if (orderBy != null)
            {
                //Validate OrderBy.
                messageSet.addMessageSet(orderByValidator.validate(searcher, orderBy));
            }
            return messageSet;
        }
        finally
        {
            UtilTimerStack.pop("DefaultSearchService.validateQuery()");
        }
    }

    @Override
    public String getJqlString(final Query query)
    {
        notNull("query", query);
        if (query.getQueryString() != null)
        {
            return query.getQueryString();
        }
        else
        {
            return getGeneratedJqlString(query);
        }
    }

    public String getGeneratedJqlString(final Query query)
    {
        notNull("query", query);

        return jqlStringSupport.generateJqlString(query);
    }

    @Override
    public Query sanitiseSearchQuery(final User searcher, final Query query)
    {
        return process(query, createClauseSanitisingVisitor(searcher));
    }

    private Query process(Query query, ClauseVisitor<Clause> visitor)
    {
        notNull("query", query);
        final Clause clause = query.getWhereClause();
        if (clause == null)
        {
            return query;
        }
        final Clause sanitisedClause = clause.accept(visitor);

        if (!clause.equals(sanitisedClause))
        {
            return new QueryImpl(sanitisedClause, query.getOrderByClause(), null);
        }
        else
        {
            return query;
        }
    }

    ///CLOVER:OFF
    ClauseSanitisingVisitor createClauseSanitisingVisitor(final User searcher)
    {
        return new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, searcher);
    }
    ///CLOVER:ON

    // We use this for testing since building a SearchContext brings up the entire world :)
    ///CLOVER:OFF
    SearchContext createSearchContext(final List<Long> projects, final List<String> issueTypes)
    {
        return new SearchContextImpl(Collections.emptyList(), projects, issueTypes);
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return factory.getInstance(user);
    }
    ///CLOVER:ON

    /**
     * Retreives all the relevant searchers to the clauses on the query.
     * <p/>
     * N.B. If ANY of the fields on the query do not have a relevant searcher, null is returned.
     *
     * @param user          the user who is performing the search
     * @param query   the SearchQuery to search for relevant searchers
     * @return the set of relevant searchers, or null if ANY of the clauses do not have a relevant searcher
     */
    Set<IssueSearcher> getRelevantQuerySearchers(final User user, Query query)
    {
        notNull("query", query);
        RelevantSearcherVisitor relevantSearcherVisitor = createRelevantSearcherVisitor(user);
        boolean allHaveSearchers = query.getWhereClause().accept(relevantSearcherVisitor);
        if (!allHaveSearchers)
        {
            return null;
        }
        else
        {
            return relevantSearcherVisitor.getRelevantSearchers();
        }
    }

    boolean calculateDoesQueryFitFilterForm(final User user, final Query query)
    {
        boolean doesItFit = true;
        // do not call the public getSearchContext method as it returns empty context instead of null
        final SearchContext simpleSearchContext = queryContextConverter.getSearchContext(getSimpleQueryContext(user, query));
        if (simpleSearchContext == null)
        {
            doesItFit = false;
        }
        else
        {
            // getRelevantQuerySearchers will only return searchers who are "shown" in the given context.
            Set<IssueSearcher> relevantSearchers = getRelevantQuerySearchers(user, query);
            if (relevantSearchers == null)
            {
                doesItFit = false;
            }
            else if (!relevantSearchers.isEmpty())
            {
                for (IssueSearcher relevantSearcher : relevantSearchers)
                {
                    // then check that the clauses fit
                    SearchInputTransformer searchInputTransformer = relevantSearcher.getSearchInputTransformer();
                    if (!searchInputTransformer.doRelevantClausesFitFilterForm(user, query, simpleSearchContext))
                    {
                        doesItFit = false;
                        break;
                    }
                }

                if (doesItFit)
                {
                    doesItFit = calculateDoesQueryValidationFitFilterForm(user, simpleSearchContext, query);
                }
            }
        }
        return doesItFit;
    }

    boolean calculateDoesQueryValidationFitFilterForm(final User user, final SearchContext searchContext, final Query query)
    {
        final ValidatorVisitor validatorVisitor = validatorVisitorFactory.createVisitor(user, null);
        TerminalClauseCollectingVisitor terminalClauseCollectingVisitor = new TerminalClauseCollectingVisitor();
        query.getWhereClause().accept(terminalClauseCollectingVisitor);

        Collection<TerminalClause> clauses = terminalClauseCollectingVisitor.getClauses();
        for (TerminalClause clause : clauses)
        {
            final Collection<IssueSearcher<?>> issueSearchers = searchHandlerManager.getSearchersByClauseName(user, clause.getName());
            for (IssueSearcher<?> issueSearcher : issueSearchers)
            {
                if (!checkValidationMatches(user, query, searchContext, clause, issueSearcher, validatorVisitor))
                {
                    return false;
                }
            }
        }
        return true;
    }

    boolean checkValidationMatches(final User user, final Query query, final SearchContext searchContext, final TerminalClause clause, final IssueSearcher searcher, final ClauseVisitor<MessageSet> validatorVisitor)
    {
        final MessageSet clauseErrors = clause.accept(validatorVisitor);
        if (!clauseErrors.hasAnyMessages())
        {
            return true;
        }

        final SearchInputTransformer transformer = searcher.getSearchInputTransformer();
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(user, holder, query, searchContext);
        ErrorCollection searcherErrors = new SimpleErrorCollection();
        transformer.validateParams(user, searchContext, holder, getI18n(user), searcherErrors);

        return searcherErrors.hasAnyErrors();
    }

    ///CLOVER:OFF
    RelevantSearcherVisitor createRelevantSearcherVisitor(final User user)
    {
        return new RelevantSearcherVisitor(searchHandlerManager, user);
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    UrlBuilder createUrlBuilder()
    {
        return new UrlBuilder(true);
    }
    ///CLOVER:ON
}
