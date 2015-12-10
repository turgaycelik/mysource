package com.atlassian.jira;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.security.random.DefaultSecureRandomService;
import com.atlassian.security.random.SecureRandomService;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Random;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default singleton production implementation.
 *
 * @since v3.13
 */
public class DefaultJiraApplicationContext implements JiraApplicationContext
{
    private final ApplicationProperties applicationProperties;
    private final JiraLicenseService jiraLicenseService;

    /**
     * In case there is no server id, we generate a random fingerprint that will be recreated when this instance is
     * reborn.
     */
    private String temporaryServerId = null;

    public DefaultJiraApplicationContext(final ApplicationProperties applicationProperties, final JiraLicenseService jiraLicenseService)
    {
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
    }

    /**
     * Generates a fingerprint based on a hash of the server id and base url to have some faith of uniqueness
     * across real instances of JIRA. Server ID alone is insufficient since it could be the same in, say, both a test
     * and production instance of JIRA if data was cloned.
     *
     * @return a 128 bit hex String representing the unique instance of JIRA.
     */
    public String getFingerPrint()
    {
        String serverId = getServerId();
        serverId = (serverId != null) ? serverId : getTemporaryServerId();
        final String baseUrl = getBaseUrl();
        // we don't care too much about null baseUrl and that won't really happen in the wild
        return generateFingerPrint(serverId, baseUrl);
    }

    String generateFingerPrint(final String serverId, final String baseUrl)
    {
        final String hash = DigestUtils.md5Hex(serverId + baseUrl);
        return hash == null ? "" : hash; // we are not allowed to return null
    }

    /**
     * Tests can override this.
     *
     * @return the server id to use if no server id is configured in JIRA.
     */
    String getTemporaryServerId()
    {
        if (temporaryServerId == null)
        {
            final SecureRandomService random = DefaultSecureRandomService.getInstance();
            temporaryServerId = Long.toString(random.nextLong());
        }
        return temporaryServerId;
    }

    String getServerId()
    {
        return jiraLicenseService.getServerId();
    }

    String getBaseUrl()
    {
        final ApplicationProperties ap = getApplicationProperties();
        return ap.getString(APKeys.JIRA_BASEURL);
    }

    ApplicationProperties getApplicationProperties()
    {
        return applicationProperties;
    }

}
