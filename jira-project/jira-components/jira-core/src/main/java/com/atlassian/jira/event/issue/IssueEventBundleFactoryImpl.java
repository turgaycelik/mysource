package com.atlassian.jira.event.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.managers.DefaultIssueDeleteHelper;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import org.ofbiz.core.entity.GenericValue;

public class IssueEventBundleFactoryImpl implements IssueEventBundleFactory
{
    private final IssueEventParamsTransformer paramsTransformer;
    private final EventTypesForIssueChange eventsForIssueChange;

    public IssueEventBundleFactoryImpl(IssueEventParamsTransformer paramsTransformer,
            EventTypesForIssueChange eventsForIssueChange)
    {
        this.paramsTransformer = paramsTransformer;
        this.eventsForIssueChange = eventsForIssueChange;
    }

    /**
     * This method uses {@link com.atlassian.jira.event.issue.EventTypesForIssueChange} to raise events during update.
     */
    @Override
    public IssueEventBundle createIssueUpdateEventBundle(Issue issue, GenericValue changeGroup, IssueUpdateBean iub,
            ApplicationUser user)
    {
        List<Long> eventIds = eventsForIssueChange.getEventTypeIdsForIssueUpdate(iub);
        Collection<IssueEvent> events = new ArrayList<IssueEvent>();
        for (Long eventId : eventIds)
        {
            IssueEvent issueEvent = new IssueEvent(
                    issue,
                    ApplicationUsers.toDirectoryUser(user),
                    iub.getComment(),
                    iub.getWorklog(),
                    changeGroup,
                    transformParams(iub.getParams()),
                    eventId,
                    iub.isSendMail(),
                    iub.isSubtasksUpdated()
            );
            events.add(issueEvent);
        }
        return wrapInBundle(events.toArray(new IssueEvent[events.size()]));
    }

    @Override
    public IssueEventBundle createWorklogEventBundle(final Issue issue, final GenericValue changeGroup, final IssueUpdateBean iub, final ApplicationUser user)
    {
        IssueEvent issueEvent = new IssueEvent(
                issue,
                ApplicationUsers.toDirectoryUser(user),
                iub.getComment(),
                iub.getWorklog(),
                changeGroup,
                transformParams(iub.getParams()),
                iub.getEventTypeId(),
                iub.isSendMail(),
                iub.isSubtasksUpdated()
        );
        return wrapInBundle(issueEvent);
    }

    @Override
    public IssueEventBundle createIssueDeleteEventBundle(Issue issue,
            DefaultIssueDeleteHelper.DeletedIssueEventData deletedIssueEventData, ApplicationUser user)
    {
        IssueEvent issueEvent = new IssueEvent(
                issue,
                transformParams(deletedIssueEventData.paramsMap()),
                ApplicationUsers.toDirectoryUser(user),
                EventType.ISSUE_DELETED_ID,
                deletedIssueEventData.isSendMail()
        );
        return wrapInBundle(issueEvent);
    }

    @Override
    public IssueEventBundle createCommentAddedBundle(Issue issue, ApplicationUser user, Comment comment,
            Map<String, Object> params)
    {
        IssueEvent issueEvent = new IssueEvent(
                issue,
                ApplicationUsers.toDirectoryUser(user),
                comment,
                null,
                null,
                transformParams(params),
                EventType.ISSUE_COMMENTED_ID
        );
        return wrapInBundle(issueEvent);
    }

    @Override
    public IssueEventBundle createCommentEditedBundle(Issue issue, ApplicationUser user, Comment comment,
            Map<String, Object> params)
    {
        IssueEvent issueEvent = new IssueEvent(
                issue,
                ApplicationUsers.toDirectoryUser(user),
                comment,
                null,
                null,
                transformParams(params),
                EventType.ISSUE_COMMENT_EDITED_ID
        );
        return wrapInBundle(issueEvent);
    }

    @Override
    public IssueEventBundle createWorkflowEventBundle(Long eventType, Issue issue, ApplicationUser user,
            Comment comment, GenericValue changeGroup, Map<String, Object> params, boolean sendMail,
            String originalAssigneeId)
    {
        IssueEvent workflowEvent = new IssueEvent(
                issue,
                ApplicationUsers.toDirectoryUser(user),
                comment,
                null,
                changeGroup,
                transformParams(params),
                eventType,
                sendMail
        );

        if (issue.getAssigneeId() == null || issue.getAssigneeId().equals(originalAssigneeId))
        {
            return wrapInBundle(workflowEvent);
        }

        IssueEvent assigneeChangedEvent = new IssueEvent(
                issue,
                ApplicationUsers.toDirectoryUser(user),
                comment,
                null,
                changeGroup,
                transformParams(params),
                EventType.ISSUE_ASSIGNED_ID,
                sendMail
        );

        return wrapInBundle(workflowEvent, assigneeChangedEvent);
    }

    @Override
    public IssueEventBundle wrapInBundle(IssueEvent issueEvent)
    {
        return wrapInBundle(new IssueEvent[] { issueEvent });
    }

    private IssueEventBundle wrapInBundle(IssueEvent... issueEvents)
    {
        Collection<JiraIssueEvent> events = new ArrayList<JiraIssueEvent>();
        for (IssueEvent issueEvent : issueEvents)
        {
            events.add(new IssueEventWrapper(issueEvent));
        }
        return DefaultIssueEventBundle.create(events);

    }

    private Map<String, Object> transformParams(Map<String, Object> params)
    {
        return paramsTransformer.transformParams(params);
    }

    private static class IssueEventWrapper implements DelegatingJiraIssueEvent
    {
        private final IssueEvent issueEvent;

        private IssueEventWrapper(IssueEvent issueEvent)
        {
            this.issueEvent = issueEvent;
        }

        @Nonnull
        @Override
        public IssueEvent asIssueEvent()
        {
            return issueEvent;
        }
    }
}
