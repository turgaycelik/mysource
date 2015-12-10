package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.AbstractAddScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class AddScheme extends AbstractAddScheme
{
    private final NotificationSchemeManager notificationSchemeManager;

    public AddScheme(final NotificationSchemeManager notificationSchemeManager)
    {
        this.notificationSchemeManager = notificationSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return notificationSchemeManager;
    }

    public String getRedirectURL()
    {
        return "EditNotifications!default.jspa?schemeId=";
    }
}
