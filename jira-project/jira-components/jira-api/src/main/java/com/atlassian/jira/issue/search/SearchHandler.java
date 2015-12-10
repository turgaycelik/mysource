package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Collections;
import java.util.List;

/**
 * Object used by the field to indicate how it can be searched and indexed.
 * <p/>
 * The field controls how JIRA indexes an issue by specifying a list of {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}s.
 * <p/>
 * Each JQL clause in JIRA is represented by a {@link com.atlassian.jira.issue.search.SearchHandler.ClauseRegistration}.
 * It consists of a set of JQL names and the {@link com.atlassian.jira.jql.ClauseHandler} that can be used to process
 * those JQL names in a query.
 * <p/>
 * Each field *may* have one {@link com.atlassian.jira.issue.search.searchers.IssueSearcher} that uses a list of JQL
 * clauses to create a JQL search. This is specified in the {@link com.atlassian.jira.issue.search.SearchHandler.SearcherRegistration}
 * on the SearchHandler. JIRA will keep the association between the IssueSearcher and JQL clauses (ClasuseHandler) to
 * perform the mapping from JQL to the GUI version of Issue Navigator. Listing ClauseHandlers in the
 * SearcherRegistration that are not related to the IssueSearcher may result JIRA falsely determining that a JQL query
 * cannot be represented on the navigator GUI. Note that the JQL clauses listed in the SearcherRegistration will also be
 * available directly in JQL.
 * <p/>
 * The field may also have another set of JQL clauses that can be used against the field but are not associated with any
 * IssueSearcher. The clauses are held in the list of ClauseRegistrations on the ClauseHandler itself. These JQL clauses
 * cannot be represented on navigtor as they have no IssueSearcher.
 * <p/>
 * The same ClauseHandler should not be listed in both the SearcherRegistration and the SearchHandler directly. Doing so
 * could result in doing JIRA performing the same search twice.
 *
 * @since v4.0
 */
@PublicApi
public final class SearchHandler
{
    private final List<ClauseRegistration> clauseRegistrations;
    private final List<FieldIndexer> fieldIndexers;
    private final SearcherRegistration searcherRegistration;

    /**
     * Create a new handler.
     *
     * @param fieldIndexers the indexers to associate with the handler. May not be null.
     * @param searcherRegistration the registration to associate with the handler. May be null.
     * @param clauseRegistrations the JQL clauses to associate with the chanler. May not be null.
     */
    public SearchHandler(final List<FieldIndexer> fieldIndexers, final SearcherRegistration searcherRegistration,
            final List<ClauseRegistration> clauseRegistrations)
    {
        this.clauseRegistrations = CollectionUtil.copyAsImmutableList(containsNoNulls("clauseRegistrations", clauseRegistrations));
        this.fieldIndexers = CollectionUtil.copyAsImmutableList(containsNoNulls("fieldIndexers", fieldIndexers));
        this.searcherRegistration = searcherRegistration;
    }

    /**
     * Create a new handler with the passed {@link com.atlassian.jira.issue.index.indexers.FieldIndexer}s and {@link
     * com.atlassian.jira.issue.search.SearchHandler.SearcherRegistration}. Same as calling {@code this(fieldIndexers,
     *searcherRegistration,Collections.<ClauseRegistration>emptyList());}
     *
     * @param fieldIndexers the indexers to associate with the handler.
     * @param searcherRegistration the searcher (and its associated clauses) to associate with the handler. May be
     * null.
     */
    public SearchHandler(final List<FieldIndexer> fieldIndexers, final SearcherRegistration searcherRegistration)
    {
        this(fieldIndexers, searcherRegistration, Collections.<ClauseRegistration>emptyList());
    }

    /**
     * The list of indexers that will JIRA will use to index the field.
     *
     * @return the list of indexers that will process this field.
     */
    public List<FieldIndexer> getIndexers()
    {
        return fieldIndexers;
    }

    /**
     * The list of JQL clauses provided for searching. These JQL clauses are not associated with any {@link
     * com.atlassian.jira.issue.search.searchers.IssueSearcher} and as such cannot be represented on the GUI version of
     * the Issue Navigator.
     *
     * @return the list of JQL clauses provided for searching. Cannot be null.
     */
    public List<ClauseRegistration> getClauseRegistrations()
    {
        return clauseRegistrations;
    }

    /**
     * The {@link com.atlassian.jira.issue.search.SearchHandler.SearcherRegistration} provided for searching.
     *
     * @return The {@link com.atlassian.jira.issue.search.SearchHandler.SearcherRegistration} provided for searching.
     *         Null may be returned to indicate that there is no IssueSearcher associated with this handler.
     */
    public SearcherRegistration getSearcherRegistration()
    {
        return searcherRegistration;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final SearchHandler that = (SearchHandler) o;

        if (!clauseRegistrations.equals(that.clauseRegistrations))
        {
            return false;
        }
        if (!fieldIndexers.equals(that.fieldIndexers))
        {
            return false;
        }
        if (searcherRegistration != null ? !searcherRegistration.equals(that.searcherRegistration) : that.searcherRegistration != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = clauseRegistrations.hashCode();
        result = 31 * result + fieldIndexers.hashCode();
        result = 31 * result + (searcherRegistration != null ? searcherRegistration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * Holds the link between an {@link com.atlassian.jira.issue.search.searchers.IssueSearcher} and the JQL clauses (as
     * {@link com.atlassian.jira.issue.search.SearchHandler.ClauseRegistration}s) that it uses in the background to
     * implement searching.  This relationship is kept within JIRA so that is can perform the JQL to Issue navigator
     * translation.
     *
     * @since 4.0
     */
    public static class SearcherRegistration
    {
        private final IssueSearcher<?> searcher;
        private final List<ClauseRegistration> clauseRegistrations;

        public SearcherRegistration(IssueSearcher<?> searcher, ClauseHandler clauseHandler)
        {
            this(searcher, new ClauseRegistration(notNull("clauseHandler",clauseHandler)));
        }

        public SearcherRegistration(final IssueSearcher<?> searcher, final List<ClauseRegistration> clauseRegistrations)
        {
            this.searcher = notNull("searcher", searcher);
            this.clauseRegistrations = CollectionUtil.copyAsImmutableList(containsNoNulls("clauseRegistrations", clauseRegistrations));
        }

        public SearcherRegistration(final IssueSearcher<?> searcher, ClauseRegistration clauseRegistration)
        {
            this(searcher, Collections.singletonList(notNull("clauseRegistration", clauseRegistration)));
        }

        public IssueSearcher<?> getIssueSearcher()
        {
            return searcher;
        }

        public List<ClauseRegistration> getClauseHandlers()
        {
            return clauseRegistrations;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final SearcherRegistration that = (SearcherRegistration) o;

            if (!clauseRegistrations.equals(that.clauseRegistrations))
            {
                return false;
            }
            if (!searcher.equals(that.searcher))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = searcher.hashCode();
            result = 31 * result + clauseRegistrations.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    /**
     * Represents a JQL clause and how to process it. Fields may use these objects to register new JQL clauses within
     * JIRA.
     *
     * @since 4.0
     */
    public static class ClauseRegistration
    {
        private final ClauseHandler handlers;

        public ClauseRegistration(final ClauseHandler handler)
        {
            this.handlers = notNull("handler", handler);
        }

        public ClauseHandler getHandler()
        {
            return handlers;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final ClauseRegistration that = (ClauseRegistration) o;

            if (!handlers.equals(that.handlers))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return handlers.hashCode();
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
