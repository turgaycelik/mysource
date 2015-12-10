package com.atlassian.jira.event.issue;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.managers.DefaultIssueDeleteHelper;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericValue;

/**
 * Factory to create {@link IssueEventBundle} objects.
 */
public interface IssueEventBundleFactory
{
    /**
     * Creates an IssueEventBundle for scenarios where an issue has been updated.
     * @param issue The updated issue
     * @param changeGroup The group of changes applied to the issue
     * @param iub The issue update bean containing all the information about the changes on the issue
     * @param user The user that made the issue update
     * @return The IssueEventBundle with all the events to be dispatched due to the issue update
     */
    IssueEventBundle createIssueUpdateEventBundle(Issue issue, GenericValue changeGroup, IssueUpdateBean iub, ApplicationUser user);

    /**
     * Creates an IssueEventBundle for scenarios where the worklog of an issue has been updated.
     * @param issue The updated issue
     * @param changeGroup The group of changes applied to the issue
     * @param iub The issue update bean containing all the information about the changes on the issue
     * @param user The user that made the issue update
     * @return The IssueEventBundle with all the events to be dispatched due to the issue update
     */
    IssueEventBundle createWorklogEventBundle(Issue issue, GenericValue changeGroup, IssueUpdateBean iub, ApplicationUser applicationUser);

    /**
     * Creates an IssueEventBundle for scenarios where an issue is deleted.
     * @param issue The deleted issue issue
     * @param deletedIssueEventData An object encapsulating information about the delete operation
     * @param user The user that deleted the issue
     * @return The IssueEventBundle with all the events to be dispatched due to the issue deletion
     */
    IssueEventBundle createIssueDeleteEventBundle(Issue issue, DefaultIssueDeleteHelper.DeletedIssueEventData deletedIssueEventData, ApplicationUser user);

    /**
     * Creates an IssueEventBundle for scenarios where a comment is added to an issue.
     * @param issue The issue for which the comment was added
     * @param user The user that added the comment
     * @param comment The comment that was added
     * @param params Some extra parameters that can be passed to create the event.
     * @return The IssueEventBundle with all the events to be dispatched due to the new comment being added
     */
    IssueEventBundle createCommentAddedBundle(Issue issue, ApplicationUser user, Comment comment, Map<String, Object> params);

    /**
     * Creates an IssueEventBundle for scenarios where a comment is edited.
     * @param issue The issue for which the comment is edited.
     * @param user The user that edited the comment
     * @param comment The comment that was edited
     * @param params Some extra parameters that can be passed to create the event.
     * @return The IssueEventBundle with all the events to be dispatched due to the comment being edited
     */
    IssueEventBundle createCommentEditedBundle(Issue issue, ApplicationUser user, Comment comment, Map<String, Object> params);

    /**
     * Creates an IssueEventBundle for scenarios where a workflow transition is executed.
     * @param eventType The type of the event
     * @param issue The issue that was transitioned
     * @param user The user that transitioned the issue
     * @param comment A comment that could have been added on the transition
     * @param changeGroup The group of changes applied to the issue
     * @param params Some extra parameters that can be passed to create the event.
     * @param sendMail A boolean indicating whether the IssueEventBundle can be sent by email
     * @param originalAssigneeId The identifier of the original assignee of the issue before the transition occurred
     * @return The IssueEventBundle with all the events to be dispatched due to the workflow transition
     */
    IssueEventBundle createWorkflowEventBundle(Long eventType, Issue issue, ApplicationUser user, Comment comment, GenericValue changeGroup, Map<String, Object> params, boolean sendMail, String originalAssigneeId);

    /**
     * Creates a IssueEventBundle that contains only one event equivalent to the given IssueEvent.
     * @param issueEvent The given issue event to be wrapped on an IssueEventBundle
     * @return An IssueEventBundle wrapping the given IssueEvent
     */
    IssueEventBundle wrapInBundle(IssueEvent issueEvent);
}
