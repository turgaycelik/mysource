package com.atlassian.jira.event.listeners.mail;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.mail.queue.MailQueueItem;

/**
 * Class in charge of generating mail notifications for issue events.
 */
public interface IssueEventMailNotifier
{
    /**
     * Creates and enqueues {@link MailQueueItem} for the recipients that should be notified of the given IssueEvent.
     * @param schemeEntities The {@link SchemeEntity} objects that will be used to extract the set of recipients.
     * @param issueEvent The {@link IssueEvent} object for which the mail queue items will be generated.
     * @param recipientsToSkip A set of recipients that should not be notified for the given issue event.
     * @return A set of the recipients notified for the given issue event.
     */
    @Nonnull
    Set<NotificationRecipient> generateNotifications(@Nonnull List<SchemeEntity> schemeEntities, @Nonnull IssueEvent issueEvent, @Nonnull Set<NotificationRecipient> recipientsToSkip);
}
