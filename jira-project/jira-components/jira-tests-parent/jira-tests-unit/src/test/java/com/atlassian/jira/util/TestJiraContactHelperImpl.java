package com.atlassian.jira.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.mail.server.MailServerManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.jira.util.JiraContactHelperImpl}.
 *
 * @since v5.1
 */
public class TestJiraContactHelperImpl
{

    @Mock private I18nHelper i18nHelper;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private MailServerManager mailServerManager;
    private final JiraProperties jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());


    @Before
    public void setUpI18n()
    {
        MockitoAnnotations.initMocks(this);
        when(i18nHelper.getText(eq(JiraContactHelperImpl.CONTACT_ADMINISTRATOR_KEY), anyString(), anyString())).thenAnswer(new Answer<String>()
        {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                return args[1] + "Contact administrators" + args[2];
            }
        });
    }

    @Test
    public void shouldReturnSimpleTextIfFormsAreOff()
    {
        when(applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM)).thenReturn(false);
        final JiraContactHelperImpl tested = new JiraContactHelperImpl(applicationProperties, mailServerManager, jiraProperties);

        assertEquals("Contact administrators", tested.getAdministratorContactLinkHtml("http://some.url", i18nHelper));
    }

    @Test
    public void shouldReturnSimpleTextIfBaseUrlIsNull()
    {
        when(applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM)).thenReturn(true);
        final JiraContactHelperImpl tested = new JiraContactHelperImpl(applicationProperties, mailServerManager, jiraProperties);

        assertEquals("Contact administrators", tested.getAdministratorContactLinkHtml(null, i18nHelper));
    }

    @Test
    public void shouldReturnLinkIfFormsTurnedOnAndBaseUrlNonNull()
    {
        when(applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM)).thenReturn(true);
        final JiraContactHelperImpl tested = new JiraContactHelperImpl(applicationProperties, mailServerManager, jiraProperties);

        assertEquals("<a href=\"http://some.url/secure/ContactAdministrators!default.jspa\">Contact administrators</a>",
                tested.getAdministratorContactLinkHtml("http://some.url", i18nHelper));
    }


    @Test
    public void testIsContactAdministratorFormEnabled()
    {
        final String atlassianMailSenddisabled = System.getProperty("atlassian.mail.senddisabled", "false");

        when(mailServerManager.isDefaultSMTPMailServerDefined()).thenReturn(false);
        when(applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM)).thenReturn(false);
        when(applicationProperties.getOption("jira.mail.send.disabled")).thenReturn(true);
        System.setProperty("atlassian.mail.senddisabled", "true");

        final JiraContactHelperImpl tested = new JiraContactHelperImpl(applicationProperties, mailServerManager, jiraProperties);

        // all conditions fail
        assertFalse(tested.isAdministratorContactFormEnabled());

        when(mailServerManager.isDefaultSMTPMailServerDefined()).thenReturn(true);
        when(applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM)).thenReturn(true);
        when(applicationProperties.getOption("jira.mail.send.disabled")).thenReturn(false);
        System.setProperty("atlassian.mail.senddisabled", "false");

        // all conditions pass
        assertTrue(tested.isAdministratorContactFormEnabled());

        System.setProperty("atlassian.mail.senddisabled", atlassianMailSenddisabled);
    }
}
