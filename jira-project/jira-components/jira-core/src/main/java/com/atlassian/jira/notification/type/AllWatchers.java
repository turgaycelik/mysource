/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;

public class AllWatchers extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(AllWatchers.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IssueManager issueManager;

    public AllWatchers(JiraAuthenticationContext jiraAuthenticationContext, IssueManager issueManager)
    {
        this.jiraAuthenticationContext = Assertions.notNull(jiraAuthenticationContext);
        this.issueManager = Assertions.notNull(issueManager);
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        try
        {
            List<ApplicationUser> watchers = getFromEventParams(event);
            if (watchers == null)
            {
                watchers = issueManager.getWatchersFor(event.getIssue());
            }
            return transform(watchers, ApplicationUserToRecipient.INSTANCE);
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings ( { "unchecked" })
    private List<ApplicationUser> getFromEventParams(IssueEvent event)
    {
        return ApplicationUsers.from((List<User>)event.getParams().get(IssueEvent.WATCHERS_PARAM_NAME));
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.all.watchers");
    }
}
