package com.atlassian.jira.issue.worklog;

import com.atlassian.crowd.embedded.api.User;

/**
 * Used to update the remaining estimate and time spent fields on an {@link com.atlassian.jira.issue.Issue} when
 * creating, updating, or deleting {@link Worklog}'s.
 */
public interface TimeTrackingIssueUpdater
{
    public static final String EVENT_ORIGINAL_WORKLOG_PARAMETER = "originalworklog";

    /**
     * Will set the {@link com.atlassian.jira.issue.worklog.Worklog#getIssue()}'s remaining estimate to the newEstimate
     * and will increment the issues time spent by the {@link com.atlassian.jira.issue.worklog.Worklog#getTimeSpent()}.
     * <br/>
     * If the newEstimate is null then the Issue's remaining estimate will not be changed.
     * <br/>
     * This method will generate change items for the updated issue fields and will update the issue's last updated
     * date.
     *
     * @param user the user performing the action, this user will be available in the dispatched event.
     * @param worklog specifies the new amount of time spent and the issue to update.
     * @param newEstimate if specified this value will over-write the Issue's remaining estimate.
     * @param dispatchEvent if true then a {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOGGED_ID} will
     * be fired, otherwise no event will be fired.
     */
    void updateIssueOnWorklogCreate(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent);

    /**
     * Will set the {@link com.atlassian.jira.issue.worklog.Worklog#getIssue()}'s remaining estimate to the newEstimate
     * and will decrement the issues time spent by the originalTimeSpent and then increment it by the
     * {@link com.atlassian.jira.issue.worklog.Worklog#getTimeSpent()}.
     * <br/>
     * If the newEstimate is null then the Issue's remaining estimate will not be changed.
     * <br/>
     * This method will generate change items for the updated issue fields and will update the issue's last updated
     * date.
     *
     * @param user the user performing the action, this user will be available in the dispatched event.
     * @param originalWorklog
     * @param newWorklog
     * @param originalTimeSpent specifies the amount of time spent that was originally specified before the worklog
     * update.
     * @param newEstimate if specified this value will over-write the Issue's remaining estimate.
     * @param dispatchEvent if true then a {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOG_UPDATED_ID} will
     */
    void updateIssueOnWorklogUpdate(User user, Worklog originalWorklog, Worklog newWorklog, Long originalTimeSpent, Long newEstimate, boolean dispatchEvent);

    /**
     * Will set the {@link com.atlassian.jira.issue.worklog.Worklog#getIssue()}'s remaining estimate to the newEstimate
     * and will decrement the issues time spent by the {@link com.atlassian.jira.issue.worklog.Worklog#getTimeSpent()}.
     * <br/>
     * If the newEstimate is null then the Issue's remaining estimate will not be changed.
     * <br/>
     * This method will generate change items for the updated issue fields, the removed worklog, and will update the
     * issue's last updated date.
     *
     * @param user the user performing the action, this user will be available in the dispatched event.
     * @param worklog specifies the amount of time spent to decrement and the issue to update.
     * @param newEstimate if specified this value will over-write the Issue's remaining estimate.
     * @param dispatchEvent if true then a {@link com.atlassian.jira.event.type.EventType#ISSUE_WORKLOG_DELETED_ID} will
     * be fired, otherwise no event will be fired.
     */
    void updateIssueOnWorklogDelete(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent);
}
