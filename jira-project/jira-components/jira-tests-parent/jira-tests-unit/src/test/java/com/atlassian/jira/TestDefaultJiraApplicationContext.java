package com.atlassian.jira;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.ApplicationProperties;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the DefaultJiraApplicationContext class.
 *
 * @since v3.13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultJiraApplicationContext
{
    @Mock private ApplicationProperties applicationProperties;
    @Mock private JiraLicenseService jiraLicenseService;

    @Test
    public void testGetFingerPrintHappy() {
        // we know the application properties are only used by the getApplicationProperties instance method.
        JiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService) {
            String getServerId()
            {
                return "12345";
            }

            String getBaseUrl()
            {
                return "http://myjira.com/";
            }

            String generateFingerPrint(String serverId, String baseUrl)
            {
                return serverId + baseUrl;
            }
        };

        assertEquals("12345http://myjira.com/", applicationContext.getFingerPrint());
    }

    @Test
    public void testGetFingerPrintNullBaseUrl() {
        // we know the application properties are only used by the getApplicationProperties instance method.
        JiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService) {
            String getServerId()
            {
                return "12345";
            }

            String getBaseUrl()
            {
                return null;
            }

            String generateFingerPrint(String serverId, String baseUrl)
            {
                return serverId + baseUrl;
            }
        };

        assertEquals("12345null", applicationContext.getFingerPrint());
    }

    @Test
    public void testGetFingerPrintNullServerId() {
        JiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService) {
            String getServerId()
            {
                return null;
            }

            String getBaseUrl()
            {
                return "http://foobar.com/jira";
            }

            String getTemporaryServerId()
            {
                return "temporary";
            }

            String generateFingerPrint(String serverId, String baseUrl)
            {
                return serverId + baseUrl;
            }
        };
        assertEquals("temporaryhttp://foobar.com/jira", applicationContext.getFingerPrint());
    }

    @Test
    public void testGenerateFingerPrint() {
        DefaultJiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService);
        String fingerPrint = applicationContext.generateFingerPrint("foo", "bar");
        assertEquals(DigestUtils.md5Hex("foobar"), fingerPrint);
    }

    @Test
    public void testGetTemporaryServerIdNotNull() {
        DefaultJiraApplicationContext applicationContext = new DefaultJiraApplicationContext(applicationProperties, jiraLicenseService);
        String tempId = applicationContext.getTemporaryServerId();
        assertNotNull(tempId);
    }
}
