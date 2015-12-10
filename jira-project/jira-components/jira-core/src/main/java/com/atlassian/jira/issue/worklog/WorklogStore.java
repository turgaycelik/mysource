package com.atlassian.jira.issue.worklog;

import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 *
 */
public interface WorklogStore
{
    /**
     * Updates fields of an existing worklog in the datastore (identified by its id) with the supplied worklog.
     * @param worklog identifies the worklog to update and provides the updated values.
     * @return the updated worklog.
     */
    Worklog update(Worklog worklog);

    /**
     * Creates a new worklog in the data store based on the values in the passed in Worklog object.
     * @param worklog specifies the values to create the worklog with.
     * @return the representation of the created worklog, including the id.
     */
    Worklog create(Worklog worklog);

    /**
     * Deletes a worklog from the data store based on the passed in id.
     * @param worklogId specifies which worklog to delete (not null)
     *
     * @return true if the worklog was deleted, false otherwise
     *
     * @throws IllegalArgumentException if the worklogId is null.
     */
    boolean delete(Long worklogId);

    /**
     * Returns a worklog specified by it's id
     *
     * @param id the specified id (not null)
     * @return the specified worklog, or null if not found
     */
    Worklog getById(Long id);

    /**
     * Returns all child worklogs of a specified issue
     *
     * @param issue the specified parent issue (not null)
     * @return a List of Worklogs, ordered by creation date. An empty List will be returned if none are found
     */
    List<Worklog> getByIssue(Issue issue);

    /**
     * Returns the count of all {@link com.atlassian.jira.issue.worklog.Worklog}'s that have a visibility restriction
     * of the provided group.
     * @param groupName identifies the group the worklogs are restricted by, this must not be null.
     * @return the count of restriced groups
     *
     * @since v3.12
     */
    long getCountForWorklogsRestrictedByGroup(String groupName);

    /**
     * Updates {@link com.atlassian.jira.issue.worklog.Worklog}'s such that worklogs that have a visibility
     * restriction of the provided groupName will be changed to have a visibility restriction of the
     * provided swapGroup.
     *
     * Note: There is no validation performed by this method to determine if the provided swapGroup is a valid
     * group with JIRA. This validation must be done by the caller.
     *
     * @param groupName identifies the group the worklogs are restricted by, this must not be null.
     * @param swapGroup identifies the group the worklogs will be changed to be restricted by, this must not be null.
     * @return tbe number of worklogs affected by the update.
     *
     * @since v3.12
     */
    int swapWorklogGroupRestriction(String groupName, String swapGroup);
}
