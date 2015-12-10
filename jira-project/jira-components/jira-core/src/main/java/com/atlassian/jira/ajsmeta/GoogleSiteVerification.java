package com.atlassian.jira.ajsmeta;

import com.atlassian.jira.config.properties.ApplicationProperties;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Utility class for including the Google site verification key in the JIRA header if it's set.
 *
 * @since v6.0
 */
public class GoogleSiteVerification
{
    public static final String GOOGLE_SITE_VERIFICATION_KEY = "google.site.verification.key";

    private final ApplicationProperties applicationProperties;

    public GoogleSiteVerification(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
    }

    /**
     * Returns the name to use in the &lt;meta&gt; element.
     *
     * @return a String containing the name to use in the &lt;meta&gt; element
     */
    public String getMetaName()
    {
        return "google-site-verification";
    }

    /**
     * Returns the Google site verification key or an empty string if it is not defined.
     *
     * @return a String containing the Google site verification key (may be empty)
     */
    public String getKey()
    {
        String propertyValue = applicationProperties.getString(GOOGLE_SITE_VERIFICATION_KEY);

        return isBlank(propertyValue) ? "" : propertyValue;
    }
}
