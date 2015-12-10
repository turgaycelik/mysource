package com.atlassian.jira.issue.worklog;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.collect.PagedList;

import java.util.List;

/**
 * The WorklogManager is used to retrieve, create, update, and remove work logs in JIRA. Worklogs are always associated 
 * with an issue.
 */
@PublicApi
public interface WorklogManager
{
    /**
     * Deletes the specified worklog and updates the issue's remaining estimate and time spent fields.
     *
     * @param user the user who is performing the action
     * @param worklog the value to remove in the database
     * @param newEstimate the value to set the {@link Issue}'s remainig estimate. If null the value will be left alone.
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOG_DELETED_ID}
     * will be dispatched and any notifications listening for that event will be triggered. If false no event will be
     * dispatched.
     * @return true if the worklog was deleted, false otherwise.
     */
    boolean delete(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent);

    /**
     * Updates the provided {@link com.atlassian.jira.issue.worklog.Worklog}. This method will
     * adjust the issue's remaining estimate to be the new value which has been passed to this method, the old
     * remaining estimate value will be lost.
     * <p/>
     * If you have provided a groupLevel then the worklog visibility will be restricted to the provided group, it is
     * assumed that validation to insure that the group actually exists has been performed outside of this method. If
     * you have provided a roleLevelId then the worklog visibility will be restricted to the provided role, it is
     * assumed that validation to insure that the role actually exists has been performed outside of this method.
     *
     * <p/>
     * NOTE: this method does not do any permission checks to see if the user can perform the requested operation.
     *
     * @param user the user who is performing the action
     * @param worklog the value to update in the database
     * @param newEstimate the value to set the {@link Issue}'s remainig estimate. If null the value will be left alone.
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOG_UPDATED_ID}
     * will be dispatched and any notifications listening for that event will be triggered. If false no event will be
     * dispatched.
     * @return the updated worklog
     */
    Worklog update(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent);

    /**
     * Creates a worklog based on the passed in {@link com.atlassian.jira.issue.worklog.Worklog} object and associates
     * it with the given issue.
     *
     * <p/>
     * If you have provided a groupLevel then the worklog visibility will be restricted to the provided group, it is
     * assumed that validation to insure that the group actually exists has been performed outside of this method. If
     * you have provided a roleLevelId then the worklog visibility will be restricted to the provided role, it is
     * assumed that validation to insure that the role actually exists has been performed outside of this method.
     *
     * <p/>
     * NOTE: this method does not do any permission checks to see if the user can perform the requested operation.
     *
     * @param user is the user who is trying to create the worklog, this can be different than the user identified by
     * {@link Worklog#getAuthor()}.
     * @param worklog the object used to provide the parameters that will be used to create the worklog.
     * @param newEstimate will be used to set the time estimate for this worklog.
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOGGED_ID}
     * will be dispatched and any notifications listening for that event will be triggered. If false no event will be
     * dispatched.
     *
     * @return a {@link Worklog} object that represents the newly created worklog.
     */
    Worklog create(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent);

    /**
     * Used to get a worklog by its id.
     * @param id uniquely identifies the worklog
     * @return returns the worklog for the passed in id, null if not found.
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
     * Returns all child worklogs of a specified issue, in a PagedList
     *
     * @param issue the specified parent issue (not null)
     * @return a List of Worklogs, ordered by creation date. An empty List will be returned if none are found
     */
    PagedList<Worklog> getByIssue(Issue issue, int pageSize);

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
    
    /**
     * This is a convenience method to allow us to easily get a ProjectRole. This is being used by the CommentImpl
     * to get a {@link ProjectRole}.
     * <p/>
     * <strong>NOTE:</strong> If you are trying to retrieve a {@link ProjectRole} then you should be using the
     * {@link com.atlassian.jira.security.roles.ProjectRoleManager}.
     * @param projectRoleId the id to the {@link ProjectRole} object you would like returned.
     * @return will return a ProjectRole based on the passed in projectRoleId.
     */
    public ProjectRole getProjectRole(Long projectRoleId);
}
