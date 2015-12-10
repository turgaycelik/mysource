package com.atlassian.jira.issue.changehistory;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages the change history of issues. TODO: expand this interface to include the functionality from ChangeLogUtils
 */
@PublicApi
public interface ChangeHistoryManager
{
    /**
     * Returns a List of  ChangeHistory entities
     *
     * @param issue the issue.
     * @return a List of ChangeHistory entries.
     */
    List<ChangeHistory> getChangeHistories(Issue issue);

    /**
     * Returns a List of ChangeHistory entities that occurred after the provided date.
     *
     * @param issue the issue. Must not be null.
     * @param since only change histories made after this date will be returned. Must not be null.
     * @return a possibly empty List of ChangeHistory entries made after the provided date. Will not be null.
     * @since v6.3
     */
    @Nonnull
    List<ChangeHistory> getChangeHistoriesSince(@Nonnull Issue issue, @Nonnull Date since);

    /**
     * Returns a List of ChangeHistory entities for a single issue.
     *
     * @param issue the issue.
     * @param remoteUser the user who is asking.
     * @return a List of ChangeHistory entries.
     * @see #getChangeHistoriesForUser(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User) for a more
     *      efficient way to read change histories for multiple issues
     */
    List<ChangeHistory> getChangeHistoriesForUser(Issue issue, User remoteUser);

    /**
     * Returns a List of ChangeHistory entities for multiple issues. This method is much more efficient than {@link
     * #getChangeHistoriesForUser(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} and should be
     * prefered whenever possible.
     *
     * @param issues the issues.
     * @param remoteUser the user who is asking.
     * @return a List of ChangeHistory entries.
     * @since 5.1
     */
    List<ChangeHistory> getChangeHistoriesForUser(Iterable<Issue> issues, User remoteUser);

    /**
     * Returns a List of ChangeItemBean's for the given issue which also are for the provided changeItemFieldName (i.e.
     * Link, Fix Version/s, etc). The order of the list will from oldest to newest.
     *
     * @param issue the issue the change items are associated with, not null.
     * @param changeItemFieldName the field name the change item is stored under, not null or empty.
     * @return a List of ChangeItemBean's for the given issue.
     */
    List<ChangeItemBean> getChangeItemsForField(Issue issue, String changeItemFieldName);

    /**
     * Returns a List of {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem}'s for the given issue
     *
     * @param issue the issue
     * @return  A list containing all of the change items for a specific Issue
     */
    List<ChangeHistoryItem> getAllChangeItems(Issue issue);

    /**
     * @return a ChangeHistory group with the given id, or null if not found
     * @since JIRA 6.3
     */
    @Nullable
    ChangeHistory getChangeHistoryById(Long changeGroupId);

    /**
     * Returns an issue that has been moved by searching on the old issue key.
     *
     * @param originalKey the original key of an issue that has since been moved (moving between projects assigns a new
     * key to an issue)
     * @return the moved {@link Issue} object
     *
     * @deprecated Use {@link com.atlassian.jira.issue.IssueManager#getIssue(String)} instead. Since v6.1.
     * @throws org.ofbiz.core.entity.GenericEntityException if an unexpected error occurs
     */
    @Deprecated
    Issue findMovedIssue(String originalKey) throws GenericEntityException;

    /**
     * Given an issue key, this method returns a list of previous issue keys this issue was moved from.  This may be
     * useful for source control plugins for example, where a given changeset should be displayed even after an issue
     * has been moved and it's issue key has changed.
     * <p/>
     * Note: The list of previous issue keys is <b>no longer</b> returned in chronological order.
     *
     * @param issueKey The current issue key.
     * @return A collection of previous issue keys or an empty list if none exist.
     *
     * @deprecated Use {@link com.atlassian.jira.issue.IssueManager#getAllIssueKeys(Long)} instead. Since v6.1.
     */
    @Deprecated
    Collection<String> getPreviousIssueKeys(String issueKey);

    /**
     * Returns the same as {@link #getPreviousIssueKeys(String)} but is slightly more efficient since no lookup of the
     * issue id needs to be performed.  If you have an issue object available with the issue's id use this method.
     *
     * @param issueId The id of the issue being looked up.
     * @return A collection of previous issue keys or an empty list if none exist.
     *
     * @deprecated Use {@link com.atlassian.jira.issue.IssueManager#getAllIssueKeys(Long)} instead. Since v6.1.
     */
    @Deprecated
    Collection<String> getPreviousIssueKeys(Long issueId);

    /**
     * Find a list of issues that the given users have acted on.
     *
     * @param remoteUser The user executing this request.
     * @param userkeys The keys of users to find the history for. If null, returns the history for all users. If empty,
     * no results will be found.
     * @param maxResults The maxmimum number of issues to return
     * @return An immutable collection of issue objects sorted by creation date in descending order
     * @since v4.0
     */
    Collection<Issue> findUserHistory(User remoteUser, Collection<String> userkeys, int maxResults);

    /**
     * Find a list of issues that the given users have acted on with the option to limit the projects included
     * in the search.
     *
     * @param remoteUser The user executing this request.
     * @param userkeys The keys of users to find the history for. If null, returns the history for all users. If empty,
     * no results will be found.
     * @param projects The projects to include issues from
     * @param maxResults The maxmimum number of issues to return
     * @return An immutable collection of issue objects sorted by creation date in descending order
     * @since v4.3
     */
    Collection<Issue> findUserHistory(User remoteUser, Collection<String> userkeys, Collection<Project> projects, int maxResults);

    /**
     * Find a map of all names ever used in the change history.
     * @deprecated since 5.2 - and will be removed in 6.0, it sucks performance wise and can kill a JIRA instance
     *              if you really need to find all values then use {@link com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder}
     * @param field The field name.
     * @return An immutable map of issue objects sorted by creation date in descending order
     * @since v4.3
     */
    Map<String, String> findAllPossibleValues(String field);

    /**
     * Remove all change items associated with an issue.
     *
     * @param issue affected issue
     */
    void removeAllChangeItems(final Issue issue);
}
