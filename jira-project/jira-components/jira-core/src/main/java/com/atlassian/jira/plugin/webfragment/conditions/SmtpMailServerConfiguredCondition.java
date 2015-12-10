package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.mail.MailFactory;

/**
 * Only displays a {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor} if
 * an SMTP mail server is configured.
 *
 * @since v5.0
 */
public class SmtpMailServerConfiguredCondition extends AbstractJiraCondition
{
    public static boolean isOutgoingMailEnabled (MailSettings mailSettings)
    {
        return !mailSettings.send().isDisabledViaApplicationProperty() &&
                MailFactory.getServerManager().getDefaultSMTPMailServer() != null;
    }

    @Override
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return SmtpMailServerConfiguredCondition.isOutgoingMailEnabled(getMailSettings());
    }

    MailSettings getMailSettings()
    {
        return ComponentAccessor.getComponent(MailSettings.class);
    }
}
