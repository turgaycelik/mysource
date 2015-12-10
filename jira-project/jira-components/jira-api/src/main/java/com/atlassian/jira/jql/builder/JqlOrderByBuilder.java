package com.atlassian.jira.jql.builder;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import net.jcip.annotations.NotThreadSafe;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to create {@link com.atlassian.query.order.OrderBy} clauses to be included in {@link
 * com.atlassian.query.Query}'s.
 *
 * The OrderBy portion of a JQL query is made up of zero of more order clauses. Each clause composes of a field
 * and either a {@link com.atlassian.query.order.SortOrder#ASC} or a {@link com.atlassian.query.order.SortOrder#DESC}
 * sort order.
 *
 * The order of sorting is from the first search clause in the list to the last. For example
 * {@code builder.status(SortOrder.DESC).component(SortOrder.ASC).buildOrderBy()} will produce the Order By statement
 * {@code Order By status DESC component ASC} which will first sort the result by status descending, and then by component
 * ascending.
 *
 * @since v4.0
 */
@NotThreadSafe
public class JqlOrderByBuilder
{
    private static final Logger log = Logger.getLogger(JqlOrderByBuilder.class);

    private List<SearchSort> searchSorts;
    private final JqlQueryBuilder parentBuilder;

    JqlOrderByBuilder(JqlQueryBuilder parentBuilder)
    {
        this.parentBuilder = parentBuilder;
        this.searchSorts = new LinkedList<SearchSort>();
    }

    /**
     * Override any sorts that may have been setup in the builder with the provided list of sorts.
     *
     * @param newSorts the new sorts to include in the builder, must not be null.
     * @return this builder.
     */
    public JqlOrderByBuilder setSorts(Collection<SearchSort> newSorts)
    {
        notNull("newSorts", newSorts);
        this.searchSorts = new LinkedList<SearchSort>(newSorts);
        return this;
    }

    /**
     * Creates a builder who's state will be a mutable copy of the passed in order by.
     *
     * @param existingOrderBy the template which defines the state the builder will be in once this method returns.
     * @return a builder who's state will be a mutable copy of the passed in order by.
     */
    public JqlOrderByBuilder setSorts(OrderBy existingOrderBy)
    {
        if (existingOrderBy != null)
        {
            this.setSorts(existingOrderBy.getSearchSorts());
        }
        return this;
    }

    /**
     *
     * Call this method to build a {@link Query} using the current builder. When {@link #endOrderBy()} is not null, this
     * equates to calling {@code endOrderBy().buildQuery()}. When {@code endOrderBy()} is null, this equates to calling
     * {@code new QueryImpl(null, buildOrderBy(), null)}.
     *
     * @throws IllegalStateException if it is not possible to build the current query given the state of the builder.
     * @return the newly generated query.
     *
     */
    public Query buildQuery()
    {
        if (parentBuilder != null)
        {
            // Create the query from our configured data
            return parentBuilder.buildQuery();
        }
        else
        {
            return new QueryImpl(null, buildOrderBy(), null);
        }
    }

    /**
     * @return the {@link com.atlassian.query.order.OrderBy} that is defined by the state of the builder.
     *         <p/>
     *         NOTE: Calling this method does not change the state of the builder, there are no limitations on the
     *         number of times this method can be invoked.
     */
    public OrderBy buildOrderBy()
    {
        return new OrderByImpl(Collections.unmodifiableList(searchSorts));
    }

    /**
     * Call this to return to the parent JqlQueryBuilder.
     *
     * @return the query builder who created this order by builder. May be null if there is no associated {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     */
    public JqlQueryBuilder endOrderBy()
    {
        return parentBuilder;
    }

    /**
     * Reset the builder to its empty state.
     *
     * @return the builder in its empty state.
     */
    public JqlOrderByBuilder clear()
    {
        searchSorts = new LinkedList<SearchSort>();
        return this;
    }

    /**
     * Add a search sort with the fieldName and the specified sort to the order by. This is a convienience method that
     * trys to lookup the primary JQL clause name for the provided field name.
     * <p/>
     * If we are unable to find the associated clause name then the sort will be added with the provided field name and
     * will likely not pass JQL validation.
     *
     * @param fieldName the field name used to lookup the JQL clause name via {@link
     * SearchHandlerManager#getJqlClauseNames(String)} method.
     * @param order the order, ASC, or DESC.
     * @param makePrimarySort if true this will be added to the beginning of the sorts, otherwise it will be added to
     * the end.
     * @return the current builder.
     */
    ///CLOVER:OFF
    public JqlOrderByBuilder addSortForFieldName(String fieldName, SortOrder order, boolean makePrimarySort)
    {
        notNull("fieldName", fieldName);
        final SearchHandlerManager searchHandlerManager = ComponentAccessor.getComponent(SearchHandlerManager.class);
        final Collection<ClauseNames> clauseNames = searchHandlerManager.getJqlClauseNames(fieldName);
        String sortName = fieldName;
        if (!clauseNames.isEmpty())
        {
            sortName = clauseNames.iterator().next().getPrimaryName();
        }
        else
        {
            log.debug("Unable to find a JQL clause name for field name '" + fieldName + "', adding sort anyway.");
        }
        return this.add(sortName, order, makePrimarySort);
    }
    ///CLOVER:ON

    /**
     * Add a search sort with the jqlClauseName and the specified sort to the order by. No validation is done in this
     * builder so you must make sure you create valid sorts.
     *
     * @param jqlClauseName the JQL clause name to sort by.
     * @param order the order, ASC, or DESC.
     * @param makePrimarySort if true this will be added to the beginning of the sorts, otherwise it will be added to
     * the end.
     * @return the current builder.
     */
    public JqlOrderByBuilder add(String jqlClauseName, SortOrder order, boolean makePrimarySort)
    {
        Assertions.notNull("jqlClauseName", jqlClauseName);
        if (makePrimarySort)
        {
            this.searchSorts.add(0, new SearchSort(jqlClauseName, order));
        }
        else
        {
            this.searchSorts.add(new SearchSort(jqlClauseName, order));
        }
        return this;
    }

    /**
     * Add a search sort with the jqlClauseName and the specified sort to the end of the sort list in the order by. No
     * validation is done in this builder so you must make sure you create valid sorts.
     * <p/>
     * This is the same as calling {@link #add(String, com.atlassian.query.order.SortOrder, boolean)} with false.
     *
     * @param jqlClauseName the JQL clause name to sort by.
     * @param order the order, ASC, or DESC.
     * @return the current builder.
     */
    public JqlOrderByBuilder add(String jqlClauseName, SortOrder order)
    {
        return this.add(jqlClauseName, order, false);
    }

    /**
     * Add a search sort with the jqlClauseName and use the claues default sort to the end of the sort list in the order
     * by. No validation is done in this builder so you must make sure you create valid sorts.
     * <p/>
     * This is the same as calling {@link #add(String, com.atlassian.query.order.SortOrder, boolean)} with null and
     * false.
     *
     * @param jqlClauseName the JQL clause name to sort by.
     * @return the current builder.
     */
    public JqlOrderByBuilder add(String jqlClauseName)
    {
        return this.add(jqlClauseName, null, false);
    }

    public JqlOrderByBuilder priority(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forPriority().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder priority(SortOrder order)
    {
        return this.priority(order, false);
    }

    public JqlOrderByBuilder currentEstimate(SortOrder order)
    {
        return this.currentEstimate(order, false);
    }

    public JqlOrderByBuilder currentEstimate(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forCurrentEstimate().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder originalEstimate(SortOrder order)
    {
        return this.originalEstimate(order, false);
    }

    public JqlOrderByBuilder originalEstimate(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forOriginalEstimate().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder votes(SortOrder order)
    {
        return this.votes(order, false);
    }

    public JqlOrderByBuilder votes(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forVotes().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder watches(SortOrder order)
    {
        return this.watches(order, false);
    }

    public JqlOrderByBuilder watches(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forWatches().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder issueKey(SortOrder order)
    {
        return this.issueKey(order, false);
    }

    public JqlOrderByBuilder issueKey(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forIssueKey().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder issueId(SortOrder order)
    {
        return this.issueId(order, false);
    }

    public JqlOrderByBuilder issueId(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forIssueId().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder project(SortOrder order)
    {
        return this.project(order, false);
    }

    public JqlOrderByBuilder project(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder issueType(SortOrder order)
    {
        return this.issueType(order, false);
    }

    public JqlOrderByBuilder issueType(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder createdDate(SortOrder order)
    {
        return this.createdDate(order, false);
    }

    public JqlOrderByBuilder createdDate(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forCreatedDate().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder dueDate(SortOrder order)
    {
        return this.dueDate(order, false);
    }

    public JqlOrderByBuilder dueDate(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forDueDate().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder lastViewedDate(SortOrder order)
    {
        return this.lastViewedDate(order, false);
    }

    public JqlOrderByBuilder lastViewedDate(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forLastViewedDate().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder updatedDate(SortOrder order)
    {
        return this.updatedDate(order, false);
    }

    public JqlOrderByBuilder updatedDate(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forUpdatedDate().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder summary(SortOrder order)
    {
        return this.summary(order, false);
    }

    public JqlOrderByBuilder summary(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder resolution(SortOrder order)
    {
        return this.resolution(order, false);
    }

    public JqlOrderByBuilder resolution(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder status(SortOrder order)
    {
        return this.status(order, false);
    }

    public JqlOrderByBuilder status(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forStatus().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder component(SortOrder order)
    {
        return this.component(order, false);
    }

    public JqlOrderByBuilder component(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forComponent().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder affectedVersion(SortOrder order)
    {
        return this.affectedVersion(order, false);
    }

    public JqlOrderByBuilder affectedVersion(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder fixForVersion(SortOrder order)
    {
        return this.fixForVersion(order, false);
    }

    public JqlOrderByBuilder fixForVersion(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder description(SortOrder order)
    {
        return this.description(order, false);
    }

    public JqlOrderByBuilder description(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder environment(SortOrder order)
    {
        return this.environment(order, false);
    }

    public JqlOrderByBuilder environment(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder resolutionDate(SortOrder order)
    {
        return this.resolutionDate(order, false);
    }

    public JqlOrderByBuilder resolutionDate(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forResolutionDate().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder reporter(SortOrder order)
    {
        return this.reporter(order, false);
    }

    public JqlOrderByBuilder reporter(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forReporter().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder assignee(SortOrder order)
    {
        return this.assignee(order, false);
    }

    public JqlOrderByBuilder assignee(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forAssignee().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder workRatio(SortOrder order)
    {
        return this.workRatio(order, false);
    }

    public JqlOrderByBuilder workRatio(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forWorkRatio().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder timeSpent(SortOrder order)
    {
        return this.timeSpent(order, false);
    }

    public JqlOrderByBuilder timeSpent(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forTimeSpent().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }

    public JqlOrderByBuilder securityLevel(SortOrder order)
    {
        return this.securityLevel(order, false);
    }

    public JqlOrderByBuilder securityLevel(SortOrder order, boolean makePrimarySort)
    {
        return this.add(SystemSearchConstants.forSecurityLevel().getJqlClauseNames().getPrimaryName(), order, makePrimarySort);
    }
}
