package com.atlassian.jira.dev.reference.plugin.notifications;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.notification.JiraNotificationReason;
import com.atlassian.jira.notification.NotificationFilter;
import com.atlassian.jira.notification.NotificationFilterContext;
import com.atlassian.jira.notification.NotificationRecipient;
import com.google.common.collect.Lists;

import java.util.Collections;

/**
 *
 */
public class ReferenceNotificationFilter implements NotificationFilter
{
    private final JiraProperties jiraSystemProperties;

    public ReferenceNotificationFilter(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }

    @Override
    public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
    {
        if (localTestEnvironment() && context.getReason().equals(JiraNotificationReason.ISSUE_EVENT))
        {
            return Lists.newArrayList(new NotificationRecipient("elvis@vegas.com"));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
    {
        return localTestEnvironment() && recipient.getEmail().startsWith("remove@");
    }

    private boolean localTestEnvironment()
    {
        return jiraSystemProperties.getBoolean("reference.local.testing");
    }
}
