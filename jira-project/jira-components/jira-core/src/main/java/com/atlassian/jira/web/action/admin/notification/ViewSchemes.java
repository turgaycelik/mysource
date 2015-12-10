package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.AbstractViewSchemes;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class ViewSchemes extends AbstractViewSchemes
{
    private final NotificationSchemeManager notificationSchemeManager;

    public ViewSchemes(final NotificationSchemeManager notificationSchemeManager)
    {
        this.notificationSchemeManager = notificationSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return notificationSchemeManager;
    }

    public String getRedirectURL()
    {
        return null;
    }
}
