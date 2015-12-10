package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.event.JiraListener;

/**
 * The IssueEventListener is the main Listener within JIRA.
 * <p/>
 * It listens to any issue related events fired within JIRA.
 *
 * @see IssueEvent
 */
@PublicSpi
public interface IssueEventListener extends JiraListener
{
    /**
     * Fired when an issue is created.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueCreated(IssueEvent event);

    /**
     * Fired when an issue is updated.
     * <p/>
     * The update changelog will be attached to the event as an action.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueUpdated(IssueEvent event);

    /**
     * Fired when an issue is assigned or unassigned.
     * <p/>
     * Check the issue assignee to work out which is the case.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueAssigned(IssueEvent event);

    /**
     * Fired when an issue is resolved.
     * <p/>
     * The resolution comment (if there is one) will be attached to the event as an action.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueResolved(IssueEvent event);

    /**
     * Fired when an issue is closed.
     * <p/>
     * The closure comment (if there is one) will be attached to the event as an action.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueClosed(IssueEvent event);

    /**
     * Fired when a user comments on an issue.
     * <p/>
     * The comment will be attached to the event as an action.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueCommented(IssueEvent event);

    /**
     * Fired when an issue is reopened.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueReopened(IssueEvent event);

    /**
     * Fired when an issue is deleted.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueDeleted(IssueEvent event);

    /**
     * Fired when an issue is moved.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueMoved(IssueEvent event);

    /**
     * Fired when work is logged against an issue.
     * <p/>
     * The work log will be attached to the event as an action.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueWorkLogged(IssueEvent event);

    /**
     * Fired when user starts work on an issue (start progress)
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueStarted(IssueEvent event);

    /**
     * Fired when user stops work on an issue (stop progress)
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueStopped(IssueEvent event);

    /**
     * Fired for all other events, eg. those fired from new workflow transitions.
     *
     * @deprecated Please call {@link #workflowEvent(IssueEvent)}
     */
    public void issueGenericEvent(IssueEvent event);

    /**
     * Fired for all events and the correct event is called
     */
    public void workflowEvent(IssueEvent event);

    /**
     * Fired for all custom events
     */
    public void customEvent(IssueEvent event);
}
