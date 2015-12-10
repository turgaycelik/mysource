package com.atlassian.jira.notification;


import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.ErrorCollection;
import com.google.common.base.Supplier;


import static com.atlassian.jira.util.dbc.Assertions.*;

/**
 * This is a simple service that allows sending issue notification to a give set of recipients that can be defined
 * arbitrarly.
 *
 * @since 5.2
 */
@PublicApi
public interface AdhocNotificationService
{
    NotificationBuilder makeBuilder();

    ValidateNotificationResult validateNotification(NotificationBuilder notification, User from, Issue issue);

    ValidateNotificationResult validateNotification(NotificationBuilder notification, User from, Issue issue, ValiationOption option);

    void sendNotification(ValidateNotificationResult result);

    static enum ValiationOption
    {
        FAIL_ON_NO_RECIPIENTS,
        CONTINUE_ON_NO_RECIPIENTS
    }

    static final class ValidateNotificationResult extends ServiceResultImpl
    {
        protected final NotificationBuilder notification;
        protected final User from;
        protected final Issue issue;
        protected final Supplier<Iterable<NotificationRecipient>> recipients;

        protected ValidateNotificationResult(ErrorCollection errorCollection, NotificationBuilder notification, Supplier<Iterable<NotificationRecipient>> recipients, User from, Issue issue)
        {
            super(errorCollection);
            this.notification = notNull(notification);
            this.from = notNull(from);
            this.issue = notNull(issue);
            this.recipients = notNull(recipients);
        }
    }
}
