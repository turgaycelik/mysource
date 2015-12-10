package com.atlassian.jira.notification;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.type.NotificationType;

/**
 * A filter context that is created for notifications in response to an Issue Event
 *
 * @since v6.0
 */
@PublicApi
public class IssueEventNotificationFilterContext extends NotificationFilterContext
{
    private final IssueEvent issueEvent;
    private final NotificationType notificationType;

    public IssueEventNotificationFilterContext(NotificationReason reason, IssueEvent issueEvent, NotificationType notificationType)
    {
        super(reason, issueEvent.getIssue());
        this.issueEvent = issueEvent;
        this.notificationType = notificationType;
    }

    public IssueEventNotificationFilterContext(IssueEventNotificationFilterContext copy, NotificationType notificationType)
    {
        super(copy);
        this.issueEvent = copy.issueEvent;
        this.notificationType = notificationType;
    }

    public IssueEvent getIssueEvent()
    {
        return issueEvent;
    }

    public NotificationType getNotificationType()
    {
        return notificationType;
    }
}
