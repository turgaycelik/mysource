package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.AbstractSelectProjectScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class SelectProjectScheme extends AbstractSelectProjectScheme
{
    private final NotificationSchemeManager notificationSchemeManager;

    public SelectProjectScheme(NotificationSchemeManager notificationSchemeManager)
    {
        this.notificationSchemeManager = notificationSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return notificationSchemeManager;
    }

    @Override
    protected String getProjectReturnUrl()
    {
        return "/plugins/servlet/project-config/" + getProjectObject().getKey() + "/notifications";
    }

}
