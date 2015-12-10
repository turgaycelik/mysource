package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.AbstractDeleteScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteScheme extends AbstractDeleteScheme
{
    private final NotificationSchemeManager notificationSchemeManager;

    public DeleteScheme(final NotificationSchemeManager notificationSchemeManager)
    {
        this.notificationSchemeManager = notificationSchemeManager;
    }

    @Override
    public SchemeManager getSchemeManager()
    {
        return notificationSchemeManager;
    }

    @Override
    public String getRedirectURL()
    {
        return "ViewNotificationSchemes.jspa";
    }
}
