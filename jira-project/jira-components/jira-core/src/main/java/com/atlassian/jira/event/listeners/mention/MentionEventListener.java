package com.atlassian.jira.event.listeners.mention;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.mention.MentionService;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;

import java.util.Set;

import static com.atlassian.jira.issue.comments.CommentManager.EVENT_ORIGINAL_COMMENT_PARAMETER;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Mention event listener that handles comment and edit events to notify any users that were mentioned using either
 *
 * @since v5.0
 */
@EventComponent
public class MentionEventListener
{
    private final MentionService mentionService;
    private final NotificationSchemeManager notificationSchemeManager;

    public MentionEventListener(MentionService mentionService, NotificationSchemeManager notificationSchemeManager)
    {
        this.notificationSchemeManager = notNull("notificationSchemeManager", notificationSchemeManager);
        this.mentionService = notNull("mentionService", mentionService);
    }

    @EventListener
    public void onIssueEvent(IssueEvent event)
    {
        final Long eventTypeId = event.getEventTypeId();

        final User remoteUser = event.getUser();

        final Set<NotificationRecipient> recipients = notificationSchemeManager.getRecipients(event);
        // if it's an event we're interested in, handle it
        if (eventTypeId.equals(EventType.ISSUE_COMMENTED_ID) || eventTypeId.equals(EventType.ISSUE_COMMENT_EDITED_ID))
        {
            //in the case of an edit, lets pass along the original comment since we can't get it any other way. Change history doesn't do comments.
            final Comment originalComment = (Comment) event.getParams().get(EVENT_ORIGINAL_COMMENT_PARAMETER);
            mentionService.sendCommentMentions(remoteUser, recipients, event.getComment(), originalComment);
        }
        else if (eventTypeId.equals(EventType.ISSUE_CREATED_ID))
        {
            mentionService.sendIssueCreateMentions(remoteUser, recipients, event.getIssue());
        }
        else if(eventTypeId.equals(EventType.ISSUE_UPDATED_ID))
        {
            mentionService.sendIssueEditMentions(remoteUser, recipients, event.getIssue(), event.getComment());
        }
        else if(eventTypeId.equals(EventType.ISSUE_COMMENT_DELETED_ID))
        {
            mentionService.sendIssueEditMentions(remoteUser, recipients, event.getIssue(), event.getComment());
        }
        else if(event.getComment() != null)
        {
            mentionService.sendCommentMentions(remoteUser, recipients, event.getComment(), null);
        }
    }

}
