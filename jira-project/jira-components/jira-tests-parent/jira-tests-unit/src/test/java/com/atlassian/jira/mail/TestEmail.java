package com.atlassian.jira.mail;

import java.util.Map;

import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the expansive {@link com.atlassian.jira.mail.Email} class.
 *
 * @since v3.13
 */
public class TestEmail
{
    @Test
    public void testHeaders()
    {
        Map headers = email().getHeaders();
        String fingerPrint = (String) headers.get(Email.HEADER_JIRA_FINGER_PRINT);
        assertEquals("fingerlicker", fingerPrint);
        assertNull(headers.get("Precedence"));
    }

    // JRA-23494
    @Test
    public void testEmailSubjectStripsLineBreaks()
    {
        assertEmailSubjectChanged("x x", "x\rx");
        assertEmailSubjectChanged("x x", "x\nx");
        assertEmailSubjectChanged("x  x", "x\r\nx");
        assertEmailSubjectChanged("x  x", "x\r\rx");
        assertEmailSubjectChanged("x   x", "x\r\n\rx");
        assertEmailSubjectChanged(" x \t x ", "\rx \t x\n");
    }

    @Test
    public void testEmailSubjectIsLeftAloneWhenItHasNoNewlines()
    {
        assertEmailSubjectUnchanged(null);
        assertEmailSubjectUnchanged("");
        assertEmailSubjectUnchanged("x\tx");
        assertEmailSubjectUnchanged("x  x");
        assertEmailSubjectUnchanged(" x \t x ");
    }



    private Email email()
    {
        final MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_EXCLUDE_PRECEDENCE_EMAIL_HEADER, true);
        Email.ConfigurationDependencies mockDeps = new MockConfigurationDependencies(mockApplicationProperties);
        return new Email("chris@example.com", mockDeps);
    }

    private void assertEmailSubjectChanged(String expected, String subject)
    {
        assertEquals(expected, email().setSubject(subject).getSubject());
    }

    private void assertEmailSubjectUnchanged(String subject)
    {
        assertEquals(subject, email().setSubject(subject).getSubject());
    }

    class MockConfigurationDependencies implements Email.ConfigurationDependencies
    {
        private final ApplicationProperties applicationProperties;

        MockConfigurationDependencies(ApplicationProperties applicationProperties)
        {
            this.applicationProperties = applicationProperties;
        }

        public ApplicationProperties getApplicationProperties()
        {
            return applicationProperties;
        }

        public JiraApplicationContext getJiraApplicationContext()
        {
            return new JiraApplicationContext() {
                public String getFingerPrint()
                {
                    return "fingerlicker";
                }
            };
        }
    }
}
