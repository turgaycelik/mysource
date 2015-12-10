package com.atlassian.jira.notification;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.base.Supplier;

public class MockAdhocNotificationService implements AdhocNotificationService
{
    public ValidateNotificationResult getSampleValidationResult(final User user, final Issue issue, final NotificationBuilder builder)
    {
        final Supplier<Iterable<NotificationRecipient>> recipients = new Supplier<Iterable<NotificationRecipient>>()
        {
            @Override
            public Iterable<NotificationRecipient> get()
            {
                return null;
            }
        };
        return new ValidateNotificationResult(new SimpleErrorCollection(), builder, recipients, user, issue);
    }

    @Override
    public NotificationBuilder makeBuilder()
    {
        return null;
    }

    @Override
    public ValidateNotificationResult validateNotification(final NotificationBuilder notification, final User from, final Issue issue)
    {
        return null;
    }

    @Override
    public ValidateNotificationResult validateNotification(final NotificationBuilder notification, final User from, final Issue issue, final ValiationOption option)
    {
        return null;
    }

    @Override
    public void sendNotification(final ValidateNotificationResult result)
    {
    }
}
