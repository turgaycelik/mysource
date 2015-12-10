package com.atlassian.jira.notification;

import com.atlassian.jira.AbstractSimpleI18NBean;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * Implementation of NotificationEvent that handles i18n of name and description.
 */
public class NotificationEventImpl extends AbstractSimpleI18NBean implements NotificationEvent
{
    public NotificationEventImpl(String id, String name, String description, String nameKey, String descriptionKey, JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(id, name, description, nameKey, descriptionKey, jiraAuthenticationContext);
    }
}
