/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SingleUser extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(SingleUser.class);
    public static final String DESC = "Single_User";

    private JiraAuthenticationContext jiraAuthenticationContext;

    public SingleUser(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String userKey)
    {
        final ApplicationUser u = getUserManager().getUserByKey(userKey);
        if (u != null)
        {
            return Collections.singletonList(new NotificationRecipient(u));
        }
        return Collections.emptyList();
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.single.user");
    }

    public String getType()
    {
        return "user";
    }

    public boolean doValidation(String key, Map parameters)
    {
        final Object value = parameters.get(key);
        if (value instanceof String)
        {
            final String displayValue = (String)value;
            return TextUtils.stringSet(displayValue) && getArgumentValue(displayValue) != null;
        }
        return false;
    }

    @Override
    public String getArgumentDisplay(String argument)
    {
        final ApplicationUser user = getUserManager().getUserByKey(argument);
        return (user != null) ? user.getUsername() : argument;
    }

    @Override
    public String getArgumentValue(String displayValue)
    {
        final ApplicationUser user = getUserManager().getUserByName(displayValue);
        return (user != null) ? user.getKey() : null;
    }

    UserManager getUserManager()
    {
        return ComponentAccessor.getUserManager();
    }
}
