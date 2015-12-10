package com.atlassian.jira.notification.type.enterprise;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.type.AbstractNotificationType;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComponentLead extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(ComponentLead.class);
    private final JiraAuthenticationContext authenticationContext;

    public ComponentLead(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public String getDisplayName()
    {
        return authenticationContext.getI18nHelper().getText("admin.projects.component.lead");
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        final Issue issue = event.getIssue();
        if (issue == null)
        {
            return Collections.emptyList();
        }

        final UserManager userManager = ComponentAccessor.getUserManager();
        final Set<NotificationRecipient> recipients = new HashSet<NotificationRecipient>();
        for (ProjectComponent component : issue.getComponentObjects())
        {
            final String leadKey = component.getLead();
            final ApplicationUser lead = userManager.getUserByKey(leadKey);
            if (lead != null)
            {
                recipients.add(new NotificationRecipient(lead));
            }
            else if (leadKey != null)
            {
                log.warn("Nonexistent user with key '" + leadKey + "' listed as component lead");
            }
        }
        return new ArrayList<NotificationRecipient>(recipients);
    }
}
