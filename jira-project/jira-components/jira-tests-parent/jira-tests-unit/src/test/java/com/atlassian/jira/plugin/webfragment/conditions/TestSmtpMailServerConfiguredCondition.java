package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.local.runner.ListeningPowerMockRunner;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.mail.settings.MailSettings.DefaultMailSettings;
import com.atlassian.jira.mail.settings.MailSettings.Send;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(MailFactory.class)
@RunWith(ListeningPowerMockRunner.class)
public class TestSmtpMailServerConfiguredCondition
{
    private MailSettings settings;
    private MailServerManager mailServerManager;
    private SMTPMailServer server;
    private ApplicationProperties applicationProperties;
    private SmtpMailServerConfiguredCondition condition;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraProperties jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());

    @Before
    public void setUp()
    {
        mailServerManager = mock(MailServerManager.class);
        server = mock(SMTPMailServer.class);
        applicationProperties = mock(ApplicationProperties.class);
        jiraAuthenticationContext = new MockSimpleAuthenticationContext(new MockUser("admin"));
        settings = new DefaultMailSettings(applicationProperties, jiraAuthenticationContext, jiraProperties);

        PowerMock.mockStaticPartial(MailFactory.class, "getSettings", "getServerManager");
        expect(MailFactory.getServerManager()).andReturn(mailServerManager).anyTimes();
        PowerMock.replay(MailFactory.class);

        condition = new SmtpMailServerConfiguredCondition()
        {
            @Override
            MailSettings getMailSettings()
            {
                return settings;
            }
        };
    }

    @Test
    public void shouldNotDisplayWhenSendingIsDisabled()
    {
        when(applicationProperties.getOption(Send.DISABLED_APPLICATION_PROPERTY)).thenReturn(true);
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(server);

        assertShouldNotDisplay();

        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(null);

        assertShouldNotDisplay();
    }

    @Test
    public void shouldNotDisplayWhenSendingIsEnabledAndServerIsNotConfigured()
    {
        when(applicationProperties.getOption(Send.DISABLED_APPLICATION_PROPERTY)).thenReturn(false);
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(null);

        assertShouldNotDisplay();
    }

    @Test
    public void shouldDisplayWhenSendingIsEnabledAndServerIsConfigured()
    {
        when(applicationProperties.getOption(Send.DISABLED_APPLICATION_PROPERTY)).thenReturn(false);
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(server);

        assertShouldDisplay();
    }

    private void assertShouldDisplay()
    {
        assertTrue(shouldDisplay());
    }

    private void assertShouldNotDisplay()
    {
        assertFalse(shouldDisplay());
    }

    private boolean shouldDisplay()
    {
        return condition.shouldDisplay(null, null);
    }
}
