package com.atlassian.jira.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.mail.server.MailServerManager;

import javax.annotation.Nullable;

/**
 * @since v4.4
 */
public class JiraContactHelperImpl implements JiraContactHelper
{
    static final String CONTACT_ADMINISTRATOR_KEY = "common.concepts.contact.administrator";
    static final String ADMINISTRATORS_LINK = "secure/ContactAdministrators!default.jspa";

    private final ApplicationProperties applicationProperties;
    private final MailServerManager mailServerManager;
    private final JiraProperties jiraSystemProperties;


    public JiraContactHelperImpl(ApplicationProperties applicationProperties,
            MailServerManager mailServerManager, JiraProperties jiraSystemProperties)
    {
        this.applicationProperties = applicationProperties;
        this.mailServerManager = mailServerManager;
        this.jiraSystemProperties = jiraSystemProperties;
    }

    @Override
    public String getAdministratorContactLinkHtml(@Nullable String baseUrl, I18nHelper i18nHelper)
    {
        if (baseUrl == null || isContactFormTurnedOff())
        {
            return getAdministratorContactMessage(i18nHelper);
        }
        final String url = getAdministratorContactLink(baseUrl);
        String link = "<a href=\"" + url + "\">";
        String closeLink = "</a>";

        return i18nHelper.getText(CONTACT_ADMINISTRATOR_KEY, link, closeLink);
    }


    private boolean isContactFormTurnedOff()
    {
        return !applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM);
    }

    @Override
    public String getAdministratorContactMessage(I18nHelper i18nHelper)
    {
        return i18nHelper.getText(CONTACT_ADMINISTRATOR_KEY, "", "");
    }

    @Override
    public boolean isAdministratorContactFormEnabled()
    {
        return mailServerManager.isDefaultSMTPMailServerDefined() &&
                applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM) &&
                !jiraSystemProperties.getBoolean("atlassian.mail.senddisabled") &&
                !applicationProperties.getOption("jira.mail.send.disabled");
    }

    @Override
    public String getAdministratorContactLink(final String baseUrl)
    {
        String url;
        if (baseUrl.endsWith("/"))
        {
            url = baseUrl + ADMINISTRATORS_LINK;
        }
        else
        {
            url = baseUrl + "/" + ADMINISTRATORS_LINK;
        }
        return url;
    }
}
