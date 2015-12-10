package com.atlassian.jira.bc.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides functionality (search, query string generation, parsing, validation, context generation, etc...) related to
 * searching in JIRA. This deals with {@link com.atlassian.query.Query}'s which contain the search criteria in JIRA.
 *
 * @since v4.0
 */
@PublicApi
public interface SearchService
{
    /**
     * Search the index, and only return issues that are in the pager's range.
     *
     * Note: that this method returns read only {@link com.atlassian.jira.issue.Issue} objects, and should not be
     * used where you need the issue for update.
     *
     * Also note that if you are after running a more complicated query see {@link com.atlassian.jira.issue.search.SearchProvider}.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search, which will be used to create a permission filter that filters out
     * any of the results the user is not able to see and will be used to provide context for the search.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     *
     * @return A {@link com.atlassian.jira.issue.search.SearchResults} containing the resulting issues.
     * @throws com.atlassian.jira.issue.search.SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     */
    SearchResults search(User searcher, Query query, PagerFilter pager) throws SearchException;

    /**
     * Search the index, and return the count of the issues matching the query.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search, which will be used to create a permission filter that filters out
     * any of the results the user is not able to see and will be used to provide context for the search.
     *
     * @return the number of issues matching the query
     * @throws com.atlassian.jira.issue.search.SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     */
    long searchCount(User searcher, Query query) throws SearchException;

    /**
     * Returns the query string to represent the specified SearchQuery.
     * <p>
     * The query string is prepended with &quot;<code>&amp;jqlQuery=</code>&quot; so that it is ready for use in building a URL.
     *
     * @param user the user performing the search
     * @param query the SearchQuery to generate the query string of. Does not accept null.
     * @return a String which represents the query string of a SearchQuery (ie no context/base applied). Never null.
     */
    String getQueryString(User user, Query query);

    /**
     * Parses the query string into a JQL {@link com.atlassian.query.Query}.
     *
     * @param searcher the user in context
     * @param query the query to parse into a {@link com.atlassian.query.Query}.
     * @return a result set that contains the query and a message set of any errors or warnings that occured during the parse.
     */
    ParseResult parseQuery(User searcher, String query);

    /**
     * Validates the specified {@link com.atlassian.query.Query} for passed user. The same as calling
     * {@code validateQuery(searcher, query, null);}.
     *
     *
     * @param searcher the user performing the search
     * @param query the search query to validate
     * @return a message set containing any errors encountered; never null.
     */
    @Nonnull
    MessageSet validateQuery(User searcher, @Nonnull Query query);

    /**
     * Validates the specified {@link com.atlassian.query.Query} for passed user and search request. This validates the
     * the passed query as if it was run as the passed search request.
     *
     * @param searcher the user performing the search.
     * @param query the search query to validate.
     * @param searchRequestId validate in the context of this search request. Can be null to indicate the passed
     * query is not currently a search request.
     *
     * @return a message set containing any errors encountered; never null.
     */
    @Nonnull
    MessageSet validateQuery(User searcher, @Nonnull Query query, Long searchRequestId);

    /**
     * Checks if a {@link com.atlassian.query.Query} is capable of being shown on the simple (GUI-based) issue navigator edit screen.
     *
     * @param user the user who is executing the query.
     * @param query the Query which to check that is displayable on the simple (GUI-based) issue navigator edit screen.
     * Does not accept null.
     * @return true if the query is displayable on the simple (GUI-based) issue navigator edit screen, false otherwise.
     */
    boolean doesQueryFitFilterForm(User user, Query query);

    /**
     * Generates a full QueryContext for the specified {@link com.atlassian.query.Query} for the searching user. The full
     * QueryContext contains all explicit and implicitly specified projects and issue types from the Query.
     * <p/>
     * For a better explanation of the differences between the full and simple QueryContexts, see
     * {@link com.atlassian.jira.jql.context.QueryContextVisitor}.
     *
     * @see com.atlassian.jira.jql.context.QueryContextVisitor
     * @param searcher the user performing the search
     * @param query the search query to generate the context for
     * @return a QueryContext that contains the implicit and explicit project / issue types implied by the included
     * clauses in the query.
     */
    QueryContext getQueryContext(User searcher, Query query);

    /**
     * Generates a simple QueryContext for the specified {@link com.atlassian.query.Query} for the searching user.
     * The simple QueryContext contains only the explicit projects and issue types specified in the Query. If none were
     * specified, it will be the Global context.
     * <p/>
     * For a better explanation of the differences between the full and simple QueryContexts, see
     * {@link com.atlassian.jira.jql.context.QueryContextVisitor}.
     *
     * @see com.atlassian.jira.jql.context.QueryContextVisitor
     * @param searcher the user performing the search
     * @param query the search query to generate the context for
     * @return a QueryContext that contains only the explicit project / issue types from the included clauses in
     * the query.
     */
    QueryContext getSimpleQueryContext(User searcher, Query query);

    /**
     * This produces an old-style {@link com.atlassian.jira.issue.search.SearchContext} based on the passed in
     * search query and the user that is performing the search.
     *
     * This will only make sense if the query returns true for {@link #doesQueryFitFilterForm(com.atlassian.crowd.embedded.api.User, com.atlassian.query.Query)}
     * since SearchContext is only relevant for simple queries.
     *
     * The more acurate context can be gotten by calling {@link #getQueryContext(com.atlassian.crowd.embedded.api.User, com.atlassian.query.Query)}.
     *
     * If the query will not fit in the simple issue navigator then the generated SearchContext will be empty. This
     * method never returns a null SearchContext, even when passed a null SearchQuery.
     *
     * @param searcher the user performing the search, not always the SearchRequest's owner
     * @param query the query for which you want a context
     * @return a SearchContext with the correct project/issue types if the query fits in the issue navigator, otherwise
     * an empty SearchContext. Never null.
     */
    SearchContext getSearchContext(User searcher, Query query);

    /**
     * Gets the JQL string representation for the passed query. Returns the string from {@link com.atlassian.query.Query#getQueryString()}
     * if it exists or generates one if it does not. Equilavent to:
     * <pre>
     *  if (query.getQueryString() != null)
     *    return query.getQueryString();
     *  else
     *    return getGeneratedJqlString(query);
     *
     * </pre>
     *
     * @param query the query. Cannot be null.
     * @return the JQL string represenation of the passed query.
     */
    String getJqlString(Query query);

    /**
     * Generates a JQL string representation for the passed query. The JQL string is always generated, that is, {@link com.atlassian.query.Query#getQueryString()}
     * is completely ignored if it exists. The returned JQL is automatically escaped as necessary.
     *
     * @param query the query. Cannot be null.
     * @return the generated JQL string representation of the passed query.
     */
    String getGeneratedJqlString(Query query);

    /**
     * Returns an equivalent {@link com.atlassian.query.Query} with all the potential "information leaks" removed,
     * with regards to the specified user. For example, if the query contains the clause "project = Invisible", and the
     * specified user does not have browse permission for project "Invisible", the sanitised query will replace this
     * clause with "project = 12345" (where 12345 is the id of the project).
     *
     * @param searcher the user performing the search
     * @param query the query to sanitise; must not be null.
     * @return the sanitised query; never null.
     */
    Query sanitiseSearchQuery(User searcher, Query query);

    @PublicApi
    public final class ParseResult
    {
        private Query query;
        private MessageSet errors;

        public ParseResult(Query query, MessageSet errors)
        {
            this.query = query;
            this.errors = notNull("errors", errors);
        }

        /**
         * @return the JQL {@link com.atlassian.query.Query} parsed, or null if the query string was not valid.
         */
        public Query getQuery()
        {
            return query;
        }

        public MessageSet getErrors()
        {
            return errors;
        }

        public boolean isValid()
        {
            return !errors.hasAnyErrors();
        }
    }
}
