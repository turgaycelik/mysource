package com.atlassian.jira.mail.settings;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.mail.Settings;

/**
 * Implements the {@link Settings} interface defined by atlassian-mail by delegating the calls to JIRA's own
 * {@link MailSettings} instance.
 *
 * @since v5.1
 */
public class ApplicationSettingsForwardingImplementation implements Settings
{
    @Override
    public boolean isSendingDisabled()
    {
        return getApplicationMailSettings().send().isDisabled();
    }

    MailSettings getApplicationMailSettings()
    {
        return ComponentAccessor.getComponent(MailSettings.class);
    }
}
