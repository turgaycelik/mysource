package com.atlassian.jira.mail.settings;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.Settings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMailSettingsSend
{
    @Mock
    private ApplicationProperties applicationProperties;

    private final JiraProperties jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        jiraAuthenticationContext = new MockSimpleAuthenticationContext(new MockUser("admin"));
    }
    
    @After
    public void resetOutgoingMailSystemProperty()
    {
        JiraSystemProperties.getInstance().unsetProperty(Settings.ATLASSIAN_MAIL_SEND_DISABLED_SYSTEM_PROPERTY_KEY);
        JiraSystemProperties.resetReferences();
    }

    @Test
    public void isEnabledIsTrueByDefault()
    {
        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertTrue
                (
                        "Outgoing mail should be enabled given that it has not been explicity disbled by either a "
                                + "system property or a JIRA application property",
                        mailSettings.send().isEnabled()
                );
    }

    @Test
    public void isDisabledIsFalseByDefault()
    {
        final MailSettings.DefaultMailSettings mailSettings =
                new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertFalse
                (
                        "Outgoing mail should be enabled given that it has not been explicity disbled by either a "
                                + "system property or a JIRA application property",
                        mailSettings.send().isDisabled()
                );
    }

    @Test
    public void isEnabledIsFalseGivenThatOutgoingMailHasBeenTurnedOffViaTheMailDisableSystemProperty()
    {
        System.setProperty(Settings.ATLASSIAN_MAIL_SEND_DISABLED_SYSTEM_PROPERTY_KEY, "true");

        final MailSettings.DefaultMailSettings mailSettings =
                new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertFalse
                (
                        "Outgoing mail should be disabled given that it has been explicity disabled by a system "
                                + "property",
                        mailSettings.send().isEnabled()
                );
    }

    @Test
    public void isDisabledIsTrueGivenThatOutgoingMailHasBeenTurnedOffViaTheMailDisableSystemProperty()
    {
        System.setProperty(Settings.ATLASSIAN_MAIL_SEND_DISABLED_SYSTEM_PROPERTY_KEY, "true");

        final MailSettings.DefaultMailSettings mailSettings =
                new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertTrue
                (
                        "Outgoing mail should be disabled given that it has been explicity disabled by a system "
                                + "property",
                        mailSettings.send().isDisabled()
                );
    }

    @Test
    public void isDisabledIsTrueGivenThatOutgoingMailHasBeenDisabledViaAnApplicationProperty()
    {
        when(applicationProperties.getOption(MailSettings.DefaultMailSettings.Send.DISABLED_APPLICATION_PROPERTY)).thenReturn(true);

        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertTrue
                (
                        "Outgoing mail should be disabled given that it has been explicity disabled by an "
                                + "application property",
                        mailSettings.send().isDisabled()
                );
    }

    @Test
    public void isEnabledIsFalseGivenThatOutgoingMailHasBeenDisabledViaAnApplicationProperty()
    {
        when(applicationProperties.getOption(MailSettings.DefaultMailSettings.Send.DISABLED_APPLICATION_PROPERTY)).thenReturn(true);

        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertFalse
                (
                        "Outgoing mail should be disabled given that it has been explicity disabled by an "
                                + "application property",
                        mailSettings.send().isEnabled()
                );
    }

    @Test
    public void disableIsSuccesfulGivenThatTheOutgoingMailSystemPropertyHasNotBeenSet()
    {
        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertTrue("It should be possible to disable outgoing mail from JIRA", mailSettings.send().disable());
        verify(applicationProperties).setOption(MailSettings.DefaultMailSettings.Send.DISABLED_APPLICATION_PROPERTY, true);
    }

    @Test
    public void disableIsNotSuccesfulGivenThatTheOutgoingMailSystemPropertyHasBeenSet()
    {
        System.setProperty(MailFactory.MAIL_DISABLED_KEY, "true");

        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertFalse
                (
                        "It should not be possible to disable outgoing mail from JIRA when the setting has already "
                                + "been set via the command line",
                        mailSettings.send().disable()
                );
        verify(applicationProperties, never()).setOption(MailSettings.DefaultMailSettings.Send.DISABLED_APPLICATION_PROPERTY, true);
    }

    @Test
    public void enableIsSuccesfulGivenThatTheOutgoingMailSystemPropertyHasNotBeenSet()
    {
        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertTrue("It should be possible to disable outgoing mail from JIRA", mailSettings.send().enable());
        verify(applicationProperties).setOption(MailSettings.DefaultMailSettings.Send.DISABLED_APPLICATION_PROPERTY, false);
    }

    @Test
    public void enableIsNotSuccesfulGivenThatTheOutgoingMailSystemPropertyHasBeenSet()
    {
        System.setProperty(MailFactory.MAIL_DISABLED_KEY, "true");

        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertFalse
                (
                        "It should not be possible to enable outgoing mail from JIRA when the setting has already "
                                + "been set via the command line",
                        mailSettings.send().enable()
                );
        verify(applicationProperties, never()).setOption(MailSettings.DefaultMailSettings.Send.DISABLED_APPLICATION_PROPERTY, false);
    }

    @Test
    public void shouldBeModifiableGivenThatTheOutgoingMailSystemPropertyHasNotBeenSet()
    {
        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertTrue(mailSettings.send().isModifiable());
    }

    @Test
    public void shouldNotBeModifiableGivenThatTheOutgoingMailSystemPropertyHasNotBeenSet()
    {
        System.setProperty(Settings.ATLASSIAN_MAIL_SEND_DISABLED_SYSTEM_PROPERTY_KEY, "true");

        final MailSettings.DefaultMailSettings mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        assertFalse(mailSettings.send().isModifiable());
    }
}
