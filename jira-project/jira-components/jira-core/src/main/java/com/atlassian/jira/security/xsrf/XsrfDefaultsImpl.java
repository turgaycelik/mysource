package com.atlassian.jira.security.xsrf;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * An implementation of {@link XsrfDefaults}
 *
 * @since v4.0
 */
public class XsrfDefaultsImpl implements XsrfDefaults
{
    private static final boolean DEFAULT_ENABLEMENT = true;
    
    private final ApplicationProperties applicationProperties;

    public XsrfDefaultsImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public boolean isXsrfProtectionEnabled()
    {
       String val = applicationProperties.getDefaultBackedString(APKeys.JIRA_XSRF_ENABLED);
       return val == null ? DEFAULT_ENABLEMENT : Boolean.valueOf(val);
    }
}
